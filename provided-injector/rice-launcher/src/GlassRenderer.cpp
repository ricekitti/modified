#include "GlassRenderer.h"
#include <d3d11.h>
#include <d3dcompiler.h>
#include <dxgi1_2.h>
#include <wrl/client.h>
#include <algorithm>
#include <cmath>
#include <cstring>

using Microsoft::WRL::ComPtr;
namespace soju {
namespace {
// A continuous, separable 49-tap Gaussian avoids stepped or duplicated edges.
constexpr char shaders[] = R"(
Texture2D scene : register(t0);
Texture2D ui : register(t1);
SamplerState linearClamp : register(s0);
cbuffer Settings : register(b0) {
    float2 texel; float2 direction;
    float2 origin; float2 scale;
    float2 size; float seconds; float unused;
    float2 pointer; float2 padding;
};
struct Vertex { float4 position : SV_POSITION; float2 uv : TEXCOORD0; };
Vertex vertex(uint id : SV_VertexID) {
    Vertex o; o.uv = float2((id << 1) & 2, id & 2);
    o.position = float4(o.uv * float2(2, -2) + float2(-1, 1), 0, 1); return o;
}
float4 gaussian(Vertex v) : SV_TARGET {
    float2 p = origin + v.uv * scale;
    float2 d = direction * texel;
    float3 c = 0; float total = 0;
    [unroll] for (int i = -24; i <= 24; ++i) {
        float weight = exp(-float(i * i) / 200.0);
        c += scene.Sample(linearClamp, p + d * i).rgb * weight;
        total += weight;
    }
    return float4(c / total, 1);
}
float4 glass(Vertex v) : SV_TARGET {
    float2 distortion = float2(sin(v.uv.y * 8 + seconds * .2), cos(v.uv.x * 7 + seconds * .15)) * .0015;
    float3 c = scene.Sample(linearClamp, v.uv + distortion).rgb;
    // A soft light veil behind the form keeps dark text readable on any wallpaper.
    float2 center = (v.uv - float2(.5, .48)) * float2(1.5, 1);
    float veil = .20 + .47 * exp(-dot(center, center) * 5);
    c = lerp(c, float3(1, .975, .985), veil);
    float2 cursor = (v.uv - pointer) * float2(size.x / size.y, 1);
    float glow = .06 * exp(-dot(cursor, cursor) * 9) + .025 * (sin(seconds * .4) + 1);
    c = lerp(c, float3(1, .68, .83), glow);
    float grain = frac(sin(dot(floor(v.position.xy), float2(12.9898, 78.233))) * 43758.5453) - .5;
    c = saturate(c + grain / 255.0);
    float4 foreground = ui.Sample(linearClamp, v.uv);
    return float4(foreground.rgb + c * (1 - foreground.a), 1);
}
)";

struct Constants {
    float texel[2], direction[2], origin[2], scale[2];
    float size[2], seconds, unused, pointer[2], padding[2];
};
static_assert(sizeof(Constants) == 64, "HLSL constant buffer layout");

struct Surface {
    ComPtr<ID3D11Texture2D> texture;
    ComPtr<ID3D11ShaderResourceView> input;
    ComPtr<ID3D11RenderTargetView> output;
};
}

struct GlassRenderer::Impl {
    HWND window = nullptr;
    int width = 0, height = 0;
    bool preview = false, frameAvailable = false;
    ULONGLONG captureRetry = 0;
    RECT monitor{};
    ComPtr<ID3D11Device> device;
    ComPtr<ID3D11DeviceContext> context;
    ComPtr<IDXGIAdapter1> adapter;
    ComPtr<IDXGISwapChain> swapchain;
    ComPtr<IDXGIOutputDuplication> capture;
    ComPtr<ID3D11VertexShader> vertex;
    ComPtr<ID3D11PixelShader> blurShader, glassShader;
    ComPtr<ID3D11SamplerState> sampler;
    ComPtr<ID3D11Buffer> constants;
    Surface desktop, horizontal, vertical, finalImage, ui;
    int sourceWidth = 0, sourceHeight = 0;

    bool surface(Surface& result, int w, int h, bool target) {
        D3D11_TEXTURE2D_DESC desc{};
        desc.Width = w; desc.Height = h; desc.MipLevels = desc.ArraySize = 1;
        desc.Format = DXGI_FORMAT_B8G8R8A8_UNORM;
        desc.SampleDesc.Count = 1;
        desc.BindFlags = D3D11_BIND_SHADER_RESOURCE | (target ? D3D11_BIND_RENDER_TARGET : 0);
        if (FAILED(device->CreateTexture2D(&desc, nullptr, result.texture.ReleaseAndGetAddressOf()))) return false;
        if (FAILED(device->CreateShaderResourceView(result.texture.Get(), nullptr, result.input.ReleaseAndGetAddressOf()))) return false;
        return !target || SUCCEEDED(device->CreateRenderTargetView(result.texture.Get(), nullptr, result.output.ReleaseAndGetAddressOf()));
    }

    bool openCapture() {
        capture.Reset();
        const HMONITOR wanted = MonitorFromWindow(window, MONITOR_DEFAULTTONEAREST);
        for (UINT i = 0;; ++i) {
            ComPtr<IDXGIOutput> output;
            if (adapter->EnumOutputs(i, &output) == DXGI_ERROR_NOT_FOUND) break;
            if (!output) continue;
            DXGI_OUTPUT_DESC desc{};
            if (FAILED(output->GetDesc(&desc)) || desc.Monitor != wanted ||
                (desc.Rotation != DXGI_MODE_ROTATION_IDENTITY && desc.Rotation != DXGI_MODE_ROTATION_UNSPECIFIED)) continue;
            ComPtr<IDXGIOutput1> output1;
            if (FAILED(output.As(&output1)) || FAILED(output1->DuplicateOutput(device.Get(), &capture))) return false;
            monitor = desc.DesktopCoordinates;
            sourceWidth = monitor.right - monitor.left; sourceHeight = monitor.bottom - monitor.top;
            frameAvailable = false;
            return surface(desktop, sourceWidth, sourceHeight, false);
        }
        return false;
    }

    bool acquire() {
        if (!capture) {
            if (GetTickCount64() < captureRetry) return false;
            captureRetry = GetTickCount64() + 1000;
            if (!openCapture()) return false;
        }
        ComPtr<IDXGIResource> resource;
        DXGI_OUTDUPL_FRAME_INFO info{};
        HRESULT hr = capture->AcquireNextFrame(0, &info, &resource);
        if (hr == DXGI_ERROR_WAIT_TIMEOUT) return frameAvailable;
        if (FAILED(hr)) { capture.Reset(); return false; }
        ComPtr<ID3D11Texture2D> texture;
        if (SUCCEEDED(resource.As(&texture))) {
            context->CopyResource(desktop.texture.Get(), texture.Get());
            frameAvailable = true;
        }
        capture->ReleaseFrame();
        return frameAvailable;
    }

    void pass(ID3D11PixelShader* shader, Surface& destination, Surface& source, const Constants& settings) {
        ID3D11ShaderResourceView* empty[2]{};
        context->PSSetShaderResources(0, 2, empty);
        ID3D11RenderTargetView* output = destination.output.Get();
        context->OMSetRenderTargets(1, &output, nullptr);
        ID3D11ShaderResourceView* inputs[] = {source.input.Get(), ui.input.Get()};
        context->PSSetShaderResources(0, 2, inputs);
        context->UpdateSubresource(constants.Get(), 0, nullptr, &settings, 0, 0);
        context->PSSetShader(shader, nullptr, 0);
        context->Draw(3, 0);
    }
};

GlassRenderer::GlassRenderer() : impl_(new Impl) {}
GlassRenderer::~GlassRenderer() = default;

bool GlassRenderer::initialize(HWND window, int width, int height, bool preview) {
    auto& p = *impl_;
    p.window = window; p.width = width; p.height = height; p.preview = preview;
    if (!preview) {
        // Exclude our own surface to prevent recursive feedback in the backdrop.
        if (!SetWindowDisplayAffinity(window, 0x11 /* WDA_EXCLUDEFROMCAPTURE */)) return false;
        ComPtr<IDXGIFactory1> factory;
        if (FAILED(CreateDXGIFactory1(IID_PPV_ARGS(&factory)))) return false;
        const HMONITOR wanted = MonitorFromWindow(window, MONITOR_DEFAULTTONEAREST);
        for (UINT i = 0; !p.adapter; ++i) {
            ComPtr<IDXGIAdapter1> adapter;
            if (factory->EnumAdapters1(i, &adapter) == DXGI_ERROR_NOT_FOUND) break;
            if (!adapter) continue;
            for (UINT j = 0;; ++j) {
                ComPtr<IDXGIOutput> output;
                if (adapter->EnumOutputs(j, &output) == DXGI_ERROR_NOT_FOUND) break;
                DXGI_OUTPUT_DESC desc{};
                if (output && SUCCEEDED(output->GetDesc(&desc)) && desc.Monitor == wanted) { p.adapter = adapter; break; }
            }
        }
        if (!p.adapter) return false;
    }
    D3D_FEATURE_LEVEL level{};
    HRESULT hr = D3D11CreateDevice(p.adapter.Get(), p.adapter ? D3D_DRIVER_TYPE_UNKNOWN : D3D_DRIVER_TYPE_HARDWARE,
        nullptr, D3D11_CREATE_DEVICE_BGRA_SUPPORT, nullptr, 0, D3D11_SDK_VERSION, &p.device, &level, &p.context);
    if (FAILED(hr) && preview)
        hr = D3D11CreateDevice(nullptr, D3D_DRIVER_TYPE_WARP, nullptr, D3D11_CREATE_DEVICE_BGRA_SUPPORT,
            nullptr, 0, D3D11_SDK_VERSION, &p.device, &level, &p.context);
    if (FAILED(hr)) return false;
    if (!preview && !p.openCapture()) return false;
    if (window) {
        ComPtr<IDXGIDevice> dxgiDevice; ComPtr<IDXGIAdapter> adapter; ComPtr<IDXGIFactory> factory;
        if (FAILED(p.device.As(&dxgiDevice)) || FAILED(dxgiDevice->GetAdapter(&adapter)) ||
            FAILED(adapter->GetParent(IID_PPV_ARGS(&factory)))) return false;
        DXGI_SWAP_CHAIN_DESC desc{};
        desc.BufferDesc.Width = width; desc.BufferDesc.Height = height;
        desc.BufferDesc.Format = DXGI_FORMAT_B8G8R8A8_UNORM;
        desc.SampleDesc.Count = 1; desc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
        desc.BufferCount = 2; desc.OutputWindow = window; desc.Windowed = TRUE;
        desc.SwapEffect = DXGI_SWAP_EFFECT_DISCARD;
        if (FAILED(factory->CreateSwapChain(p.device.Get(), &desc, &p.swapchain))) return false;
        factory->MakeWindowAssociation(window, DXGI_MWA_NO_ALT_ENTER);
    }
    ComPtr<ID3DBlob> code, errors;
    const UINT flags = D3DCOMPILE_ENABLE_STRICTNESS | D3DCOMPILE_OPTIMIZATION_LEVEL3;
    if (FAILED(D3DCompile(shaders, sizeof(shaders), nullptr, nullptr, nullptr, "vertex", "vs_4_0", flags, 0, &code, &errors)) ||
        FAILED(p.device->CreateVertexShader(code->GetBufferPointer(), code->GetBufferSize(), nullptr, &p.vertex))) return false;
    code.Reset(); errors.Reset();
    if (FAILED(D3DCompile(shaders, sizeof(shaders), nullptr, nullptr, nullptr, "gaussian", "ps_4_0", flags, 0, &code, &errors)) ||
        FAILED(p.device->CreatePixelShader(code->GetBufferPointer(), code->GetBufferSize(), nullptr, &p.blurShader))) return false;
    code.Reset(); errors.Reset();
    if (FAILED(D3DCompile(shaders, sizeof(shaders), nullptr, nullptr, nullptr, "glass", "ps_4_0", flags, 0, &code, &errors)) ||
        FAILED(p.device->CreatePixelShader(code->GetBufferPointer(), code->GetBufferSize(), nullptr, &p.glassShader))) return false;
    D3D11_SAMPLER_DESC sample{};
    sample.Filter = D3D11_FILTER_MIN_MAG_MIP_LINEAR;
    sample.AddressU = sample.AddressV = sample.AddressW = D3D11_TEXTURE_ADDRESS_CLAMP;
    sample.MaxLOD = D3D11_FLOAT32_MAX;
    if (FAILED(p.device->CreateSamplerState(&sample, &p.sampler))) return false;
    D3D11_BUFFER_DESC buffer{}; buffer.ByteWidth = sizeof(Constants); buffer.BindFlags = D3D11_BIND_CONSTANT_BUFFER;
    if (FAILED(p.device->CreateBuffer(&buffer, nullptr, &p.constants))) return false;
    if (!p.surface(p.horizontal, width, height, true) || !p.surface(p.vertical, width, height, true) ||
        !p.surface(p.finalImage, width, height, true) || !p.surface(p.ui, width, height, false)) return false;
    if (preview) {
        p.sourceWidth = width; p.sourceHeight = height;
        if (!p.surface(p.desktop, width, height, false)) return false;
        // A synthetic backdrop for design exports and tests. Never samples a user's desktop.
        std::vector<unsigned char> pattern(static_cast<size_t>(width) * height * 4);
        for (int y = 0; y < height; ++y) for (int x = 0; x < width; ++x) {
            const size_t i = (static_cast<size_t>(y) * width + x) * 4;
            if (width <= 256) {
                pattern[i] = pattern[i + 1] = pattern[i + 2] = ((x / 4 + y / 4) & 1) ? 255 : 0;
            } else {
                float u = static_cast<float>(x) / width, v = static_cast<float>(y) / height;
                pattern[i] = static_cast<unsigned char>(155 + 65 * u);
                pattern[i + 1] = static_cast<unsigned char>(105 + 95 * v);
                pattern[i + 2] = static_cast<unsigned char>(150 + 65 * (1 - u));
                if (x % 130 < 22 || y % 100 < 8) for (int c = 0; c < 3; ++c) pattern[i + c] = static_cast<unsigned char>(pattern[i + c] * .55);
            }
            pattern[i + 3] = 255;
        }
        p.context->UpdateSubresource(p.desktop.texture.Get(), 0, nullptr, pattern.data(), width * 4, 0);
    }
    return true;
}

bool GlassRenderer::render(const void* uiPixels, float seconds, POINT pointer) {
    auto& p = *impl_;
    if (!p.device || !p.glassShader || !uiPixels) return false;
    if (!p.preview && !p.acquire()) return false;
    D3D11_VIEWPORT viewport{0, 0, static_cast<float>(p.width), static_cast<float>(p.height), 0, 1};
    p.context->RSSetViewports(1, &viewport);
    p.context->IASetPrimitiveTopology(D3D11_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
    p.context->VSSetShader(p.vertex.Get(), nullptr, 0);
    auto* sampler = p.sampler.Get(); p.context->PSSetSamplers(0, 1, &sampler);
    auto* constants = p.constants.Get(); p.context->PSSetConstantBuffers(0, 1, &constants);
    p.context->UpdateSubresource(p.ui.texture.Get(), 0, nullptr, uiPixels, p.width * 4, 0);
    Constants settings{};
    settings.texel[0] = 1.f / p.sourceWidth; settings.texel[1] = 1.f / p.sourceHeight;
    settings.direction[0] = 1;
    settings.scale[0] = settings.scale[1] = 1;
    if (!p.preview) {
        POINT origin{}; ClientToScreen(p.window, &origin);
        settings.origin[0] = static_cast<float>(origin.x - p.monitor.left) / p.sourceWidth;
        settings.origin[1] = static_cast<float>(origin.y - p.monitor.top) / p.sourceHeight;
        settings.scale[0] = static_cast<float>(p.width) / p.sourceWidth;
        settings.scale[1] = static_cast<float>(p.height) / p.sourceHeight;
    }
    p.pass(p.blurShader.Get(), p.horizontal, p.desktop, settings);
    settings.origin[0] = settings.origin[1] = 0;
    settings.scale[0] = settings.scale[1] = 1;
    settings.texel[0] = 1.f / p.width; settings.texel[1] = 1.f / p.height;
    settings.direction[0] = 0; settings.direction[1] = 1;
    p.pass(p.blurShader.Get(), p.vertical, p.horizontal, settings);
    settings.size[0] = static_cast<float>(p.width); settings.size[1] = static_cast<float>(p.height);
    settings.seconds = seconds;
    settings.pointer[0] = static_cast<float>(pointer.x) / p.width;
    settings.pointer[1] = static_cast<float>(pointer.y) / p.height;
    p.pass(p.glassShader.Get(), p.finalImage, p.vertical, settings);
    ID3D11ShaderResourceView* empty[2]{}; p.context->PSSetShaderResources(0, 2, empty);
    p.context->OMSetRenderTargets(0, nullptr, nullptr);
    if (p.swapchain) {
        ComPtr<ID3D11Texture2D> buffer;
        if (FAILED(p.swapchain->GetBuffer(0, IID_PPV_ARGS(&buffer)))) return false;
        p.context->CopyResource(buffer.Get(), p.finalImage.texture.Get());
        if (FAILED(p.swapchain->Present(0, 0))) return false;
    }
    return true;
}

bool GlassRenderer::readback(std::vector<unsigned char>& pixels, bool blurOnly) {
    auto& p = *impl_;
    // Readback is used by explicit synthetic previews/tests only, never for desktop frames.
    if (!p.preview || !p.finalImage.texture) return false;
    ID3D11Texture2D* source = blurOnly ? p.vertical.texture.Get() : p.finalImage.texture.Get();
    D3D11_TEXTURE2D_DESC desc{}; source->GetDesc(&desc);
    desc.BindFlags = desc.MiscFlags = 0; desc.Usage = D3D11_USAGE_STAGING; desc.CPUAccessFlags = D3D11_CPU_ACCESS_READ;
    ComPtr<ID3D11Texture2D> staging;
    if (FAILED(p.device->CreateTexture2D(&desc, nullptr, &staging))) return false;
    p.context->CopyResource(staging.Get(), source);
    D3D11_MAPPED_SUBRESOURCE mapped{};
    if (FAILED(p.context->Map(staging.Get(), 0, D3D11_MAP_READ, 0, &mapped))) return false;
    pixels.resize(static_cast<size_t>(p.width) * p.height * 4);
    for (int y = 0; y < p.height; ++y)
        std::memcpy(pixels.data() + static_cast<size_t>(y) * p.width * 4,
            static_cast<const BYTE*>(mapped.pData) + static_cast<size_t>(y) * mapped.RowPitch, p.width * 4);
    p.context->Unmap(staging.Get(), 0);
    return true;
}
}
