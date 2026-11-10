#define WIN32_LEAN_AND_MEAN
#define NOMINMAX
#include <windows.h>
#include <GL/gl.h>
#include <cstring>
#include <fstream>
#include <iterator>
#include <iostream>
#include <stdexcept>
#include <vector>
#include <cstdarg>
#include "../../../slinkyreadablesource/slinkyreadablesource/src/logic/visual/heavy_glow_gl.cpp"
#include "../../../slinkyreadablesource/slinkyreadablesource/src/logic/visual/heavy_glow.cpp"
#include "../../../slinkyreadablesource/slinkyreadablesource/src/engine/modlib/e_3c.cpp"
static void check(bool ok,const char* why){if(!ok)throw std::runtime_error(why);}
static bool limitUnits=false,noShaders=false;
static void capabilityGet(unsigned name,int* value){if(limitUnits&&name==0x8872){*value=2;return;}glGetIntegerv(name,value);}
extern "C" void* slinky_gl_get_proc(const char* name){if(noShaders&&std::strcmp(name,"glCreateShader")==0)return nullptr;
    if(std::strcmp(name,"glGetIntegerv")==0)return (void*)capabilityGet;auto p=(void*)wglGetProcAddress(name);
    if(!p||p==(void*)1||p==(void*)2||p==(void*)3||p==(void*)-1)p=(void*)GetProcAddress(GetModuleHandleW(L"opengl32.dll"),name);return p;}
extern "C" void slinky_logf(const char* format,...){va_list a;va_start(a,format);vfprintf(stderr,format,a);va_end(a);fputc('\n',stderr);}
namespace heavyglowclient {
extern "C" {alignas(8) char data_2e9e80[0x210]={};alignas(8) char data_2ea0e0[0x40]={};unsigned data_2ea110=2;
    float data_2ea1d0=100;unsigned char data_2ea250=1,data_2ea270=0,data_2ea158=0;int data_2ea2c0=1;
    Color data_2ea1e0={1,0,0},data_2ea200={0,1,0},data_2ea220={1,0,1};}
}
namespace fixture {
static int world,player,other,friendly,mc,renderer,list,settings;static GLuint white;
static bool outlines=false,hiddenGui=false;
static int models=0;static bool shadow=true,pending=false,throwModel=false,capturedName=false;
static bool cacheTest=false;static int cachedActive=0x84c0;static int cachedTextures[32]={};
static bool wrongRenderer=false;
enum Field {PosX=1,PosY,PosZ,Dead,Hurt,Width,Height,Outlines,HideGui,Settings};
enum Method {Render=100,GetShadow,SetShadow,BindTexture,ActiveTexture,DepthMask,AlphaFunc,BlendFunc,Color,
    EnableDepth,DisableDepth,EnableLighting,DisableLighting,EnableBlend,DisableBlend,EnableCull,DisableCull,EnableAlpha,DisableAlpha,EnableTexture,DisableTexture};
static void* id(int n){return (void*)(size_t)n;}static int n(void* p){return (int)(size_t)p;}
static jint push(JNIEnv*,jint){return 0;}static jobject pop(JNIEnv*,jobject p){return p;}
static void local(JNIEnv*,jobject){}static jboolean exception(JNIEnv*){return pending;}static void clearException(JNIEnv*){pending=false;}
static jboolean same(JNIEnv*,jobject a,jobject b){return a==b;}static jboolean instance(JNIEnv*,jobject p,jclass cls){if(cls==id(777))return p==&renderer;return p==&other||p==&friendly||p==&player;}
static jboolean getBool(JNIEnv*,jobject,jfieldID f){return n(f)==Outlines?outlines:n(f)==HideGui?hiddenGui:false;}
static void setBool(JNIEnv*,jobject,jfieldID f,jboolean value){if(n(f)==Outlines)outlines=value!=0;else if(n(f)==HideGui)hiddenGui=value!=0;}
static jobject getObject(JNIEnv*,jobject,jfieldID f){return n(f)==Settings?&settings:nullptr;}
static jdouble getDouble(JNIEnv*,jobject o,jfieldID f){return n(f)==PosZ&&o!=&player?5:0;}
static jint getInt(JNIEnv*,jobject,jfieldID f){return n(f)==Width||n(f)==Height?64:0;}
static void rectangle(double x,double y,double w,double h){glBegin(GL_QUADS);glTexCoord2f(.5f,.5f);glVertex2d(x,y);glVertex2d(x+w,y);glVertex2d(x+w,y+h);glVertex2d(x,y+h);glEnd();}
static void model(){if(!cacheTest||cachedTextures[cachedActive-0x84c0]!=(int)white)glBindTexture(GL_TEXTURE_2D,white);if(cacheTest)cachedTextures[cachedActive-0x84c0]=(int)white;rectangle(28,42,8,10);rectangle(27,23,10,20);rectangle(22,27,5,15);rectangle(37,27,5,15);rectangle(27,12,4,12);rectangle(33,12,4,12);}
static jboolean callBool(JNIEnv*,jobject,jmethodID method,...){if(n(method)==GetShadow)return shadow;
    if(n(method)==Render){++models;check(heavy_glow_capture_active(),"Name suppression must cover the model render callback");
        check(outlines&&hiddenGui,"Capture must enable model-only outlines and suppress client-added GUI labels");
        // Simulate a client label that bypasses vanilla's canRenderName hook.
        capturedName|=!outlines&&!hiddenGui;
        capturedName|=should_render_player_name(nullptr,&renderer,&other)!=nullptr;
        if(throwModel){pending=true;return false;}model();return true;}return false;}
static void callVoid(JNIEnv*,jobject,jmethodID method,...){if(n(method)==SetShadow){va_list a;va_start(a,method);shadow=va_arg(a,int)!=0;va_end(a);}}
static void callStatic(JNIEnv*,jclass,jmethodID method,...){va_list a;va_start(a,method);int m=n(method);
    if(m==ActiveTexture){int unit=va_arg(a,int);if(cachedActive!=unit)heavyglow::gl.active((unsigned)unit);cachedActive=unit;}
    else if(m==BindTexture){int texture=va_arg(a,int);if(cachedTextures[cachedActive-0x84c0]!=texture)glBindTexture(GL_TEXTURE_2D,(GLuint)texture);cachedTextures[cachedActive-0x84c0]=texture;}
    else if(m==DepthMask)glDepthMask(va_arg(a,int)!=0);
    else if(m==AlphaFunc){int f=va_arg(a,int);double ref=va_arg(a,double);glAlphaFunc((GLenum)f,(float)ref);}
    else if(m==BlendFunc){int s=va_arg(a,int),d=va_arg(a,int),sa=va_arg(a,int),da=va_arg(a,int);heavyglow::gl.blendSeparate(s,d,sa,da);}
    else if(m==Color){float r=(float)va_arg(a,double),g=(float)va_arg(a,double),b=(float)va_arg(a,double),alpha=(float)va_arg(a,double);glColor4f(r,g,b,alpha);}
    else {unsigned cap=m==EnableDepth||m==DisableDepth?GL_DEPTH_TEST:m==EnableLighting||m==DisableLighting?GL_LIGHTING:m==EnableBlend||m==DisableBlend?GL_BLEND:
        m==EnableCull||m==DisableCull?GL_CULL_FACE:m==EnableAlpha||m==DisableAlpha?GL_ALPHA_TEST:GL_TEXTURE_2D;
        bool on=m==EnableDepth||m==EnableLighting||m==EnableBlend||m==EnableCull||m==EnableAlpha||m==EnableTexture;if(on)glEnable(cap);else glDisable(cap);}
    va_end(a);
}
extern "C" {void* data_2fc8c0=nullptr;char data_2fc8c8[8]={};int data_2fc8cc=0;unsigned char data_2f02e4=0;}
extern "C" void vector_erase(void*,int,void*,void*){}
extern "C" void* java_jni_is_instance_of_entity_player(void*,void* entity){return entity;}
extern "C" unsigned char entity_flags_has(void*,void* entity,int mask){return entity==&fixture::friendly&&(mask&2);}
extern "C" void* java_call_renderer_living_entity(void*,void*,void*){return (void*)1;}
}
namespace heavyglowclient {
extern "C" jobject java_get_minecraft_6(Jvm*,jobject){return fixture::wrongRenderer?&fixture::world:&fixture::renderer;}
extern "C" jobject java_get_world(Jvm*,jobject){return &fixture::list;}
extern "C" int java_call_list_size(Jvm*,jobject){return 3;}
extern "C" jobject java_call_list_get(Jvm*,jobject,int n){return n==0?&fixture::player:n==1?&fixture::other:&fixture::friendly;}
extern "C" unsigned entity_flags_get(Jvm*,jobject p){return p==&fixture::friendly?2:0;}
extern "C" jobject get_refill_state(){return nullptr;}
extern "C" int entity_name_colour(Jvm*,jobject){return 15;}
extern "C" Color hex_digit_value(int){return {1,1,1};}
}
static bool shape(void*,void(*color)(float,float,float)){color(1,0,1);fixture::model();return true;}
static bool shapeWithLightmap(void*,void(*color)(float,float,float)){
    color(1,0,1);fixture::model();
    // Minecraft/Lunar model layers may leave the lightmap texture environment
    // enabled. A fixed-function fullscreen copy must not inherit this state.
    heavyglow::gl.active(0x84c1);glEnable(GL_TEXTURE_2D);glTexEnvi(GL_TEXTURE_ENV,GL_TEXTURE_ENV_MODE,GL_REPLACE);
    glMatrixMode(GL_TEXTURE);glScalef(2,3,1);glMatrixMode(GL_MODELVIEW);
    auto multi=(void(*)(unsigned,float,float))slinky_gl_get_proc("glMultiTexCoord2f");multi(0x84c1,.5f,.5f);
    heavyglow::gl.active(0x84c0);return true;
}
int main(int argc,char** argv){HWND window=nullptr;HDC dc=nullptr;HGLRC context=nullptr;try{
    limitUnits=argc>1&&std::strcmp(argv[1],"--two-units")==0;noShaders=argc>1&&std::strcmp(argv[1],"--no-shaders")==0;
    struct Shader {const char* name;const char* text;};Shader shaders[]={{"VERTEX",openb4::VERTEX},{"OUTLINE_MASK",openb4::OUTLINE_MASK},{"OUTLINE_CAPTURE_VERTEX",openb4::OUTLINE_CAPTURE_VERTEX},
        {"KAWASE_BLOOM_DOWN",openb4::KAWASE_BLOOM_DOWN},{"KAWASE_BLOOM_UP",openb4::KAWASE_BLOOM_UP},{"OUTLINE",openb4::OUTLINE}};
    for(auto s:shaders){std::ifstream file(std::string(OPENB4_REFERENCE_DIR)+"/"+s.name+".glsl",std::ios::binary);
        check(file.good(),"Missing shader reference");std::string reference((std::istreambuf_iterator<char>(file)),{});check(reference==s.text,"Shader bytes differ from OpenB4 source");}
    heavy_glow_extend_modes();auto choice=(heavyglowclient::Choice*)heavyglowclient::data_2ea0e0;
    check(choice->count==3&&std::strcmp(choice->labels[0],"2D")==0&&std::strcmp(choice->labels[1],"3D")==0&&std::strcmp(choice->labels[2],"Heavy Glow")==0,"ESP must preserve old mode indices and append Heavy Glow");
    check(std::strcmp(heavy_glow_mode_name(2),"Heavy Glow")==0,"Status must safely resolve the third mode");
    WNDCLASSW cls={};cls.lpfnWndProc=DefWindowProcW;cls.hInstance=GetModuleHandleW(nullptr);cls.lpszClassName=L"RiceHeavyGlowTest";cls.style=CS_OWNDC;RegisterClassW(&cls);
    window=CreateWindowW(cls.lpszClassName,L"Heavy Glow GPU test",WS_POPUP,0,0,256,256,nullptr,nullptr,cls.hInstance,nullptr);if(!window)return 77;
    dc=GetDC(window);PIXELFORMATDESCRIPTOR pfd={};pfd.nSize=sizeof(pfd);pfd.nVersion=1;pfd.dwFlags=PFD_DRAW_TO_WINDOW|PFD_SUPPORT_OPENGL|PFD_DOUBLEBUFFER;
    pfd.iPixelType=PFD_TYPE_RGBA;pfd.cColorBits=32;pfd.cAlphaBits=8;int format=ChoosePixelFormat(dc,&pfd);if(!format||!SetPixelFormat(dc,format,&pfd))return 77;
    context=wglCreateContext(dc);if(!context||!wglMakeCurrent(dc,context))return 77;
    if(noShaders){check(!heavy_glow_draw(64,64,shape,nullptr)&&!heavyglow::capture.fb,"Missing shader support must return cleanly for standard ESP fallback");
        wglMakeCurrent(nullptr,nullptr);wglDeleteContext(context);ReleaseDC(window,dc);DestroyWindow(window);std::cout<<"Unsupported GPU fallback signal passed\n";return 0;}
    if(!heavyglow::resolve()){check(!limitUnits,"Heavy Glow must support two fragment texture units");std::cout<<"SKIP: driver lacks OpenB4 shader requirements\n";return 77;}
    std::cout<<"OpenGL: "<<glGetString(GL_VERSION)<<"; "<<glGetString(GL_RENDERER)<<'\n';
    glGenTextures(1,&fixture::white);glBindTexture(GL_TEXTURE_2D,fixture::white);unsigned char white[]={255,255,255,255};glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,1,1,0,GL_RGBA,GL_UNSIGNED_BYTE,white);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
    glViewport(0,0,64,64);glMatrixMode(GL_PROJECTION);glLoadIdentity();glOrtho(0,64,0,64,-1,1);glMatrixMode(GL_MODELVIEW);glLoadIdentity();
    glClearColor(0,0,0,1);glClear(GL_COLOR_BUFFER_BIT);check(heavy_glow_draw(64,64,shape,nullptr),"Heavy Glow render failed");
    check(glGetError()==GL_NO_ERROR,"Heavy Glow left an OpenGL error");
    std::vector<unsigned char> pixels(64*64*4);glReadPixels(0,0,64,64,GL_RGBA,GL_UNSIGNED_BYTE,pixels.data());int glowing=0;
    for(int y=0;y<64;++y)for(int x=0;x<64;++x)if((x<22||x>42||y<12||y>52)&&pixels[(y*64+x)*4]>0)++glowing;
    check(glowing>30,"Heavy Glow must produce a halo outside the captured silhouette");
    check(pixels[(32*64+32)*4]==0,"Heavy Glow must preserve the silhouette interior instead of filling it");
    glClearColor(.2f,.4f,.6f,1);glClear(GL_COLOR_BUFFER_BIT);glDisable(GL_TEXTURE_2D);glColor4f(.7f,.3f,.1f,1);fixture::rectangle(27,23,10,20);glEnable(GL_TEXTURE_2D);
    unsigned char before[4]={},after[4]={};glReadPixels(32,32,1,1,GL_RGBA,GL_UNSIGNED_BYTE,before);
    check(heavy_glow_draw(64,64,shapeWithLightmap,nullptr),"Lightmapped model capture failed");glReadPixels(32,32,1,1,GL_RGBA,GL_UNSIGNED_BYTE,after);
    check(std::abs((int)before[0]-after[0])<=1&&std::abs((int)before[1]-after[1])<=1&&std::abs((int)before[2]-after[2])<=1&&before[3]==after[3],
        "Glow must preserve colored player pixels and scene alpha when model layers leave a lightmap enabled");
    heavyglow::gl.active(0x84c1);float textureMatrix[16]={};glGetFloatv(GL_TEXTURE_MATRIX,textureMatrix);heavyglow::gl.active(0x84c0);
    check(textureMatrix[0]==1&&textureMatrix[5]==1,"Model lightmap texture matrix must be restored between frames");
    auto oldTexture=heavyglow::capture.texture;glViewport(3,4,60,55);glEnable(GL_DEPTH_TEST);glDisable(GL_BLEND);heavyglow::gl.active(0x84c3);glMatrixMode(GL_TEXTURE);
    check(heavy_glow_draw(128,96,shape,nullptr),"Framebuffer resize failed");int view[4]={},active=0,matrix=0;glGetIntegerv(GL_VIEWPORT,view);glGetIntegerv(0x84e0,&active);glGetIntegerv(GL_MATRIX_MODE,&matrix);
    check(view[0]==3&&view[1]==4&&view[2]==60&&view[3]==55&&active==0x84c3&&matrix==GL_TEXTURE&&glIsEnabled(GL_DEPTH_TEST)&&!glIsEnabled(GL_BLEND),"Rendering must restore viewport, active texture, matrices, depth, and blend state");
    // Drivers may immediately reuse deleted texture names; inspect actual storage.
    heavyglow::gl.active(0x84c0);glBindTexture(GL_TEXTURE_2D,heavyglow::capture.texture);int tw=0,th=0;
    glGetTexLevelParameteriv(GL_TEXTURE_2D,0,GL_TEXTURE_WIDTH,&tw);glGetTexLevelParameteriv(GL_TEXTURE_2D,0,GL_TEXTURE_HEIGHT,&th);
    check(tw==128&&th==96&&heavyglow::buffers[1].w==64&&heavyglow::buffers[2].h==24,"Resize must recreate full, half, and quarter resolution storage");
    heavyglow::gl.active(0x84c0);glMatrixMode(GL_PROJECTION);glLoadIdentity();glOrtho(0,64,0,64,-1,1);glMatrixMode(GL_MODELVIEW);glLoadIdentity();glViewport(0,0,64,64);
    JNINativeInterface_ api={};api.PushLocalFrame=fixture::push;api.PopLocalFrame=fixture::pop;api.DeleteLocalRef=fixture::local;api.ExceptionCheck=fixture::exception;api.ExceptionClear=fixture::clearException;
    api.IsSameObject=fixture::same;api.IsInstanceOf=fixture::instance;api.GetBooleanField=fixture::getBool;api.SetBooleanField=fixture::setBool;api.GetObjectField=fixture::getObject;api.GetDoubleField=fixture::getDouble;api.GetIntField=fixture::getInt;api.CallBooleanMethod=fixture::callBool;api.CallVoidMethod=fixture::callVoid;
    api.CallStaticVoidMethod=fixture::callStatic;
    JNIEnv env=&api;JIds ids={};Jvm j={&ids,&env};ids.Entity_posX=fixture::id(fixture::PosX);ids.Entity_posY=fixture::id(fixture::PosY);ids.Entity_posZ=fixture::id(fixture::PosZ);
    ids.Minecraft_displayWidth=fixture::id(fixture::Width);ids.Minecraft_displayHeight=fixture::id(fixture::Height);
    ids.RenderManager=fixture::id(777);
    ids.Minecraft_Lbbj_=fixture::id(fixture::Settings);
    heavyglowclient::renderOutlines=fixture::id(fixture::Outlines);heavyglowclient::hideGui=fixture::id(fixture::HideGui);
    ids.GlStateManager=fixture::id(999);ids.GlStateManager_color=fixture::id(fixture::Color);
    ids.GlStateManager_enableDepth=fixture::id(fixture::EnableDepth);ids.GlStateManager_disableDepth=fixture::id(fixture::DisableDepth);
    ids.GlStateManager_enableLighting=fixture::id(fixture::EnableLighting);ids.GlStateManager_disableLighting=fixture::id(fixture::DisableLighting);
    ids.GlStateManager_enableBlend=fixture::id(fixture::EnableBlend);ids.GlStateManager_disableBlend=fixture::id(fixture::DisableBlend);
    ids.GlStateManager_enableCull=fixture::id(fixture::EnableCull);ids.GlStateManager_disableCull=fixture::id(fixture::DisableCull);
    ids.GlStateManager_enableAlpha=fixture::id(fixture::EnableAlpha);ids.GlStateManager_disableAlpha=fixture::id(fixture::DisableAlpha);
    ids.GlStateManager_enableTexture2D=fixture::id(fixture::EnableTexture);ids.GlStateManager_disableTexture2D=fixture::id(fixture::DisableTexture);
    heavyglowclient::bindTexture=fixture::id(fixture::BindTexture);heavyglowclient::activeTexture=fixture::id(fixture::ActiveTexture);
    heavyglowclient::depthMask=fixture::id(fixture::DepthMask);heavyglowclient::alphaFunc=fixture::id(fixture::AlphaFunc);heavyglowclient::blendFunc=fixture::id(fixture::BlendFunc);
    GLuint originalTexture=0;glGenTextures(1,&originalTexture);glBindTexture(GL_TEXTURE_2D,originalTexture);glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,1,1,0,GL_RGBA,GL_UNSIGNED_BYTE,white);
    fixture::cacheTest=true;fixture::cachedTextures[0]=(int)originalTexture;
    heavyglowclient::renderEntity=fixture::id(fixture::Render);heavyglowclient::getShadow=fixture::id(fixture::GetShadow);heavyglowclient::setShadow=fixture::id(fixture::SetShadow);
    heavyglowclient::Context c={&j,nullptr,nullptr,&fixture::mc,&fixture::world,&fixture::player};heavyglowclient::Render event={6,{},&c,1};heavy_glow_render(&event);
    check(fixture::models==1&&fixture::shadow,"Native capture must filter self/friends and restore renderer shadows");
    check(!fixture::capturedName&&!heavy_glow_capture_active(),"Glow must exclude nametags and restore capture scope afterward");
    check(!fixture::outlines&&!fixture::hiddenGui,"Normal world HUD and outline state must be restored after capture");
    check(should_render_player_name(nullptr,&fixture::renderer,&fixture::other)!=nullptr,"Normal world nametags must remain available outside capture");
    data_2f02e4=1;check(!should_render_player_name(nullptr,&fixture::renderer,&fixture::other),"Existing No Name Tags setting must still be respected");data_2f02e4=0;
    int bound=0;glGetIntegerv(GL_TEXTURE_BINDING_2D,&bound);check(bound==(int)originalTexture&&fixture::cachedTextures[0]==bound,"Java texture cache must match restored GL binding after capture");
    fixture::wrongRenderer=true;int previousModels=fixture::models;
    check(!heavy_glow_render(&event)&&fixture::models==previousModels&&fixture::shadow,"Wrong renderer class must be rejected before JNI dispatch and signal classic ESP fallback");
    fixture::wrongRenderer=false;
    fixture::throwModel=true;heavy_glow_render(&event);check(fixture::shadow&&!fixture::pending,"Throwing player renderer must restore shadows and clear JNI exception");
    check(!fixture::outlines&&!fixture::hiddenGui,"Throwing renderer must restore label/HUD and outline state");
    check(!heavy_glow_capture_active()&&should_render_player_name(nullptr,&fixture::renderer,&fixture::other)!=nullptr,"Renderer failure must not leave normal nametags suppressed");
    auto texture=heavyglow::capture.texture;auto descriptor=(heavyglowclient::Descriptor*)heavyglowclient::data_2e9e80;
    heavyglowclient::Toggle toggle={5,&c,descriptor,0};descriptor->events[5].fn(&toggle);check(!glIsTexture(texture)&&heavyglow::fill==0,"Disabling ESP must release GPU targets and shader programs");
    check(!heavy_glow_draw(0,0,shape,nullptr),"Zero-size window must skip rendering");
    glDeleteTextures(1,&fixture::white);glDeleteTextures(1,&originalTexture);wglMakeCurrent(nullptr,nullptr);wglDeleteContext(context);ReleaseDC(window,dc);DestroyWindow(window);
    std::cout<<"Byte-identical shaders, GPU halo, interior mask, resize/state restoration, native filtering, exceptions, and cleanup passed\n";return 0;
}catch(const std::exception& e){std::cerr<<e.what()<<'\n';if(context){wglMakeCurrent(nullptr,nullptr);wglDeleteContext(context);}if(window){ReleaseDC(window,dc);DestroyWindow(window);}return 1;}}
