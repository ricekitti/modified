#pragma once
#include <string>
namespace soju {
// HTTPS form POST to the existing KeyAuth app endpoint. Never logs form data.
std::string keyAuthPost(const std::string& form,std::wstring& error);
}
