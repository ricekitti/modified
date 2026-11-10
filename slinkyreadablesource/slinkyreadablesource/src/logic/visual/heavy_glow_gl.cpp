// OpenB4 u20+-4: unchanged mask, capture vertex, Kawase bloom, and outline GLSL.
#include "slinky/heavy_glow.h"
#include "slinky/openb4_shaders.h"
#include <stddef.h>
extern "C" void* slinky_gl_get_proc(const char*);
extern "C" void slinky_logf(const char*,...);
namespace heavyglow {
using U=unsigned;using I=int;using F=float;
struct GL {
    void(*get)(U,I*);void(*push)(U);void(*pop)();void(*pushClient)(U);void(*popClient)();
    void(*enable)(U);void(*disable)(U);void(*depth)(unsigned char);void(*alpha)(U,F);void(*blend)(U,U);void(*blendSeparate)(U,U,U,U);
    void(*clearColor)(F,F,F,F);void(*clear)(U);void(*viewport)(I,I,I,I);void(*color)(F,F,F,F);void(*colorMask)(unsigned char,unsigned char,unsigned char,unsigned char);
    void(*matrix)(U);void(*pushMatrix)();void(*popMatrix)();void(*identity)();void(*ortho)(double,double,double,double,double,double);
    void(*begin)(U);void(*end)();void(*coord)(F,F);void(*vertex)(double,double);
    void(*active)(U);void(*bindTex)(U,U);void(*genTex)(I,U*);void(*deleteTex)(I,const U*);void(*param)(U,U,I);
    void(*image)(U,I,I,I,I,I,U,U,const void*);
    void(*genFB)(I,U*);void(*deleteFB)(I,const U*);void(*bindFB)(U,U);void(*attach)(U,U,U,U,I);U(*status)(U);
    U(*createShader)(U);void(*source)(U,I,const char*const*,const I*);void(*compile)(U);void(*shaderInfo)(U,U,I*);void(*deleteShader)(U);
    U(*createProgram)();void(*attachShader)(U,U);void(*link)(U);void(*programInfo)(U,U,I*);void(*deleteProgram)(U);void(*use)(U);
    I(*location)(U,const char*);void(*uniform1i)(I,I);void(*uniform1f)(I,F);void(*uniform2f)(I,F,F);void(*uniform3f)(I,F,F,F);
};
static GL gl;static bool ready,failed,coreFB;
struct Target {U fb,texture;I w,h;};
static Target capture,buffers[3];static U fill,down,up,outline,composite;
// The final copy must not run through Minecraft's fixed-function lightmap /
// texture-combiner state. Keep the source scene and its alpha inside the mask.
static constexpr char compositeFragment[]=R"GLSL(#version 120
uniform sampler2D glow_texture;
uniform sampler2D mask_texture;
void main() {
    vec2 uv = gl_TexCoord[0].st;
    if (texture2D(mask_texture, uv).a > 0.1) discard;
    vec4 glow = texture2D(glow_texture, uv);
    if (glow.a <= 0.0) discard;
    gl_FragColor = glow;
}
)GLSL";
static void* proc(const char* name,const char* fallback=nullptr){auto p=slinky_gl_get_proc(name);return p?p:fallback?slinky_gl_get_proc(fallback):nullptr;}
static bool resolve(){if(failed)return false;if(ready)return true;
#define GET(field,name) gl.field=(decltype(gl.field))proc(name);if(!gl.field){failed=true;return false;}
    GET(get,"glGetIntegerv") GET(push,"glPushAttrib") GET(pop,"glPopAttrib") GET(pushClient,"glPushClientAttrib") GET(popClient,"glPopClientAttrib")
    GET(enable,"glEnable") GET(disable,"glDisable") GET(depth,"glDepthMask") GET(alpha,"glAlphaFunc") GET(blend,"glBlendFunc") GET(blendSeparate,"glBlendFuncSeparate")
    GET(clearColor,"glClearColor") GET(clear,"glClear") GET(viewport,"glViewport") GET(color,"glColor4f") GET(colorMask,"glColorMask")
    GET(matrix,"glMatrixMode") GET(pushMatrix,"glPushMatrix") GET(popMatrix,"glPopMatrix") GET(identity,"glLoadIdentity") GET(ortho,"glOrtho")
    GET(begin,"glBegin") GET(end,"glEnd") GET(coord,"glTexCoord2f") GET(vertex,"glVertex2d") GET(active,"glActiveTexture")
    GET(bindTex,"glBindTexture") GET(genTex,"glGenTextures") GET(deleteTex,"glDeleteTextures") GET(param,"glTexParameteri") GET(image,"glTexImage2D")
    GET(createShader,"glCreateShader") GET(source,"glShaderSource") GET(compile,"glCompileShader") GET(shaderInfo,"glGetShaderiv") GET(deleteShader,"glDeleteShader")
    GET(createProgram,"glCreateProgram") GET(attachShader,"glAttachShader") GET(link,"glLinkProgram") GET(programInfo,"glGetProgramiv") GET(deleteProgram,"glDeleteProgram") GET(use,"glUseProgram")
    GET(location,"glGetUniformLocation") GET(uniform1i,"glUniform1i") GET(uniform1f,"glUniform1f") GET(uniform2f,"glUniform2f") GET(uniform3f,"glUniform3f")
#undef GET
#define FB(field,name) gl.field=(decltype(gl.field))proc(name,name "EXT");if(!gl.field){failed=true;return false;}
    coreFB=proc("glBindFramebuffer")!=nullptr;
    FB(genFB,"glGenFramebuffers") FB(deleteFB,"glDeleteFramebuffers") FB(bindFB,"glBindFramebuffer") FB(attach,"glFramebufferTexture2D") FB(status,"glCheckFramebufferStatus")
#undef FB
    I units=0;gl.get(0x8872,&units);if(units<2){failed=true;slinky_logf("Heavy Glow: shader unavailable; using standard ESP");return false;}
    ready=true;return true;
}
static U program(const char* fragment,const char* vertex){U f=gl.createShader(0x8b30),v=gl.createShader(0x8b31),p=0;I good=0;
    if(f&&v){gl.source(f,1,&fragment,nullptr);gl.source(v,1,&vertex,nullptr);gl.compile(f);gl.compile(v);
        I a=0,b=0;gl.shaderInfo(f,0x8b81,&a);gl.shaderInfo(v,0x8b81,&b);
        if(a&&b){p=gl.createProgram();gl.attachShader(p,f);gl.attachShader(p,v);gl.link(p);gl.programInfo(p,0x8b82,&good);}}
    if(f)gl.deleteShader(f);if(v)gl.deleteShader(v);if(p&&!good){gl.deleteProgram(p);p=0;}return p;
}
static void destroy(Target& t){if(t.fb)gl.deleteFB(1,&t.fb);if(t.texture)gl.deleteTex(1,&t.texture);t={};}
static bool target(Target& t,int w,int h,bool smooth){
    gl.genTex(1,&t.texture);gl.bindTex(0xde1,t.texture);gl.param(0xde1,0x2801,smooth?0x2601:0x2600);gl.param(0xde1,0x2800,smooth?0x2601:0x2600);
    gl.param(0xde1,0x2802,smooth?0x8370:0x2900);gl.param(0xde1,0x2803,smooth?0x8370:0x2900);gl.image(0xde1,0,0x8058,w,h,0,0x1908,0x1401,nullptr);
    gl.genFB(1,&t.fb);gl.bindFB(0x8d40,t.fb);gl.attach(0x8d40,0x8ce0,0xde1,t.texture,0);t.w=w;t.h=h;
    return t.texture&&t.fb&&gl.status(0x8d40)==0x8cd5;
}
static void quad(int w,int h){gl.begin(7);gl.coord(0,1);gl.vertex(0,0);gl.coord(0,0);gl.vertex(0,h);gl.coord(1,0);gl.vertex(w,h);gl.coord(1,1);gl.vertex(w,0);gl.end();}
static void uniform(U p,const char* name,I value){gl.uniform1i(gl.location(p,name),value);}
static void uniform(U p,const char* name,F value){gl.uniform1f(gl.location(p,name),value);}
static void pair(U p,const char* name,F x,F y){gl.uniform2f(gl.location(p,name),x,y);}
static void color(F r,F g,F b){gl.use(fill);uniform(fill,"u_texture",0);uniform(fill,"u_alpha_threshold",.001f);gl.uniform3f(gl.location(fill,"u_color"),r,g,b);}
static void bind(Target& t){gl.bindFB(0x8d40,t.fb);gl.viewport(0,0,t.w,t.h);gl.clearColor(0,0,0,0);gl.clear(0x4000);}
static void pass(Target& out,U texture,U shader,int w,int h,bool check){
    bind(out);gl.use(shader);gl.active(0x84c0);gl.bindTex(0xde1,texture);uniform(shader,"inTexture",0);uniform(shader,"check",check?1:0);
    pair(shader,"offset",6.5f,6.5f);pair(shader,"halfpixel",1.f/out.w,1.f/out.h);pair(shader,"iResolution",(F)out.w,(F)out.h);
    // Same GLSL, using sampler 1 instead of OpenB4's sampler 16 for older GPUs.
    if(check){uniform(shader,"textureToCheck",1);gl.active(0x84c1);gl.bindTex(0xde1,capture.texture);gl.active(0x84c0);}
    quad(w,h);gl.use(0);
}
struct Saved {I draw,read,viewport[4],program,matrix,active,texture0,textureAux;};
static Saved save(){Saved s={};gl.get(0x8ca6,&s.draw);s.read=s.draw;if(coreFB)gl.get(0x8caa,&s.read);
    gl.get(0xba2,s.viewport);gl.get(0x8b8d,&s.program);gl.get(0xba0,&s.matrix);gl.get(0x84e0,&s.active);
    gl.active(0x84c0);gl.get(0x8069,&s.texture0);gl.active(0x84c1);gl.get(0x8069,&s.textureAux);gl.active((U)s.active);
    gl.push(0xfffff);gl.pushClient(0xffffffff);gl.matrix(0x1701);gl.pushMatrix();gl.matrix(0x1700);gl.pushMatrix();
    gl.active(0x84c0);gl.matrix(0x1702);gl.pushMatrix();gl.active(0x84c1);gl.pushMatrix();gl.active(0x84c0);gl.matrix(0x1700);return s;
}
static void restore(const Saved& s){gl.use((U)s.program);gl.active(0x84c0);gl.bindTex(0xde1,(U)s.texture0);gl.active(0x84c1);gl.bindTex(0xde1,(U)s.textureAux);
    gl.matrix(0x1702);gl.popMatrix();gl.active(0x84c0);gl.popMatrix();
    gl.matrix(0x1700);gl.popMatrix();gl.matrix(0x1701);gl.popMatrix();gl.matrix((U)s.matrix);
    if(coreFB){gl.bindFB(0x8ca9,(U)s.draw);gl.bindFB(0x8ca8,(U)s.read);}else gl.bindFB(0x8d40,(U)s.draw);
    gl.popClient();gl.pop();gl.active((U)s.active);gl.viewport(s.viewport[0],s.viewport[1],s.viewport[2],s.viewport[3]);}
}
void heavy_glow_release(){using namespace heavyglow;if(!ready)return;destroy(capture);for(auto& t:buffers)destroy(t);
    U programs[]={fill,down,up,outline,composite};for(auto p:programs)if(p)gl.deleteProgram(p);fill=down=up=outline=composite=0;
}
bool heavy_glow_draw(int w,int h,HeavyGlowDraw draw,void* context){using namespace heavyglow;
    if(w<4||h<4||!draw||!resolve())return false;auto saved=save();bool ok=true;
    gl.active(0x84c0);
    if(!fill){fill=program(openb4::OUTLINE_MASK,openb4::OUTLINE_CAPTURE_VERTEX);down=program(openb4::KAWASE_BLOOM_DOWN,openb4::VERTEX);
        up=program(openb4::KAWASE_BLOOM_UP,openb4::VERTEX);outline=program(openb4::OUTLINE,openb4::VERTEX);
        composite=program(compositeFragment,openb4::VERTEX);ok=fill&&down&&up&&outline&&composite;
        if(!ok){heavy_glow_release();failed=true;slinky_logf("Heavy Glow: OpenB4 GLSL compilation/link failed");}}
    if(ok&&(capture.w!=w||capture.h!=h)){
        destroy(capture);for(auto& t:buffers)destroy(t);
        ok=target(capture,w,h,false)&&target(buffers[0],w,h,false)&&target(buffers[1],w/2,h/2,true)&&target(buffers[2],w/4,h/4,true);
        if(!ok){heavy_glow_release();failed=true;slinky_logf("Heavy Glow: framebuffer allocation failed");}}
    bool drew=false;
    if(ok){gl.disable(0xc11);gl.colorMask(1,1,1,1);bind(capture);gl.disable(0xb50);gl.disable(0xbe2);gl.enable(0xbc0);gl.alpha(0x204,.001f);gl.depth(0);
        gl.active(0x84c1);gl.disable(0xde1);gl.active(0x84c0);gl.enable(0xde1);gl.color(1,1,1,1);
        drew=draw(context,color);gl.use(0);
    }
    if(ok&&drew){
        gl.matrix(0x1701);gl.identity();gl.ortho(0,w,h,0,-1,1);gl.matrix(0x1700);gl.identity();
        gl.disable(0xb71);gl.disable(0xb50);gl.disable(0xb44);gl.disable(0xc11);gl.depth(0);gl.enable(0xde1);gl.enable(0xbc0);gl.alpha(0x204,0);
        gl.enable(0xbe2);gl.blend(1,1);gl.color(1,1,1,1);
        pass(buffers[1],capture.texture,down,w,h,false);pass(buffers[2],buffers[1].texture,down,w,h,false);
        pass(buffers[1],buffers[2].texture,up,w,h,false);pass(buffers[0],buffers[1].texture,up,w,h,true);
        gl.bindFB(0x8d40,(U)saved.draw);gl.viewport(saved.viewport[0],saved.viewport[1],saved.viewport[2],saved.viewport[3]);
        gl.active(0x84c1);gl.disable(0xde1);gl.bindTex(0xde1,capture.texture);gl.active(0x84c0);gl.bindTex(0xde1,buffers[0].texture);
        gl.blendSeparate(0x302,0x303,0,1);gl.use(composite);uniform(composite,"glow_texture",0);uniform(composite,"mask_texture",1);quad(w,h);
        gl.use(outline);uniform(outline,"u_texture",0);pair(outline,"u_texel_size",1.f/w,1.f/h);
        uniform(outline,"u_radius",2.f);uniform(outline,"u_alpha_threshold",.1f);gl.bindTex(0xde1,capture.texture);quad(w,h);
    }
    restore(saved);return ok;
}
