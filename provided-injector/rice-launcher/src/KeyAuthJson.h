#pragma once
#include <string>
#include <map>
#include <set>
#include <stdexcept>
#include <cstdlib>

namespace soju::keyauth {
// Parse the complete response before inspecting authentication fields. Nested
// success flags, duplicate keys, strings, and truncated objects cannot log in.
struct JsonValue {
    enum Kind { Other, String, Boolean } kind=Other;
    std::string text;
    bool boolean=false;
};
class JsonResponse {
    const std::string& input;
    size_t pos=0;
    std::map<std::string,JsonValue> fields;
    [[noreturn]] void invalid() { throw std::runtime_error("Invalid KeyAuth JSON"); }
    void space() { while(pos<input.size() && (input[pos]==' '||input[pos]=='\t'||input[pos]=='\r'||input[pos]=='\n')) ++pos; }
    bool take(char c) { space(); if(pos<input.size()&&input[pos]==c) {++pos;return true;} return false; }
    unsigned hex4() {
        unsigned n=0;
        for(int i=0;i<4;++i) {
            if(pos==input.size()) invalid();
            char c=input[pos++];unsigned d;
            if(c>='0'&&c<='9') d=c-'0';
            else if(c>='a'&&c<='f') d=c-'a'+10;
            else if(c>='A'&&c<='F') d=c-'A'+10;
            else { invalid(); }
            n=(n<<4)|d;
        }
        return n;
    }
    static void utf8(std::string& out,unsigned n) {
        if(n<0x80) out+=static_cast<char>(n);
        else if(n<0x800) {out+=static_cast<char>(0xc0|(n>>6));out+=static_cast<char>(0x80|(n&63));}
        else if(n<0x10000) {out+=static_cast<char>(0xe0|(n>>12));out+=static_cast<char>(0x80|((n>>6)&63));out+=static_cast<char>(0x80|(n&63));}
        else {out+=static_cast<char>(0xf0|(n>>18));out+=static_cast<char>(0x80|((n>>12)&63));out+=static_cast<char>(0x80|((n>>6)&63));out+=static_cast<char>(0x80|(n&63));}
    }
    std::string string() {
        if(!take('"')) invalid();
        std::string out;
        while(pos<input.size()) {
            unsigned char c=input[pos++];
            if(c=='"') return out;
            if(c<32) invalid();
            if(c!='\\') {out+=static_cast<char>(c);continue;}
            if(pos==input.size()) invalid();
            switch(input[pos++]) {
            case '"':out+='"';break;case '\\':out+='\\';break;case '/':out+='/';break;
            case 'b':out+='\b';break;case 'f':out+='\f';break;case 'n':out+='\n';break;case 'r':out+='\r';break;case 't':out+='\t';break;
            case 'u': {
                unsigned n=hex4();
                if(n>=0xd800&&n<=0xdbff) {
                    if(pos+2>input.size()||input[pos++]!='\\'||input[pos++]!='u') invalid();
                    unsigned low=hex4();if(low<0xdc00||low>0xdfff) invalid();
                    n=0x10000+((n-0xd800)<<10)+(low-0xdc00);
                } else if(n>=0xdc00&&n<=0xdfff) invalid();
                utf8(out,n);break;
            }
            default:invalid();
            }
        }
        invalid();
    }
    JsonValue value(unsigned depth) {
        if(depth>32) invalid();
        space();if(pos==input.size()) invalid();
        JsonValue out;
        if(input[pos]=='"') {out.kind=JsonValue::String;out.text=string();return out;}
        if(take('{')) {object(depth+1,false);return out;}
        if(take('[')) {if(!take(']')) {do{value(depth+1);}while(take(','));if(!take(']'))invalid();}return out;}
        for(auto literal : {"true","false","null"}) {
            const size_t size=std::char_traits<char>::length(literal);
            if(input.compare(pos,size,literal)==0) {pos+=size;if(literal[0]!='n'){out.kind=JsonValue::Boolean;out.boolean=literal[0]=='t';}return out;}
        }
        size_t start=pos;
        if(input[pos]=='-') ++pos;
        if(pos==input.size()) invalid();
        if(input[pos]=='0') ++pos;
        else {if(input[pos]<'1'||input[pos]>'9')invalid();while(pos<input.size()&&input[pos]>='0'&&input[pos]<='9')++pos;}
        if(pos<input.size()&&input[pos]=='.') {++pos;digits();}
        if(pos<input.size()&&(input[pos]=='e'||input[pos]=='E')) {++pos;if(pos<input.size()&&(input[pos]=='+'||input[pos]=='-'))++pos;digits();}
        if(pos==start) invalid();return out;
    }
    void digits() {size_t start=pos;while(pos<input.size()&&input[pos]>='0'&&input[pos]<='9')++pos;if(pos==start)invalid();}
    void object(unsigned depth,bool root) {
        std::set<std::string> keys;
        if(take('}')) return;
        do {
            auto key=string();if(!keys.insert(key).second||!take(':'))invalid();
            auto v=value(depth);if(root)fields.emplace(key,std::move(v));
        }while(take(','));
        if(!take('}'))invalid();
    }
public:
    explicit JsonResponse(const std::string& body):input(body) {if(!take('{'))invalid();object(0,true);space();if(pos!=input.size())invalid();}
    bool success() const {auto p=fields.find("success");return p!=fields.end()&&p->second.kind==JsonValue::Boolean&&p->second.boolean;}
    std::string string(const char* key) const {auto p=fields.find(key);return p!=fields.end()&&p->second.kind==JsonValue::String?p->second.text:"";}
};
}

