#pragma once

#include <string>
#include <vector>

#include <userver/formats/json_fwd.hpp>

namespace tour_guide {

struct User {
    int id;
    std::string login;
    std::string password;
};

userver::formats::json::Value Serialize(const User& user, userver::formats::serialize::To<userver::formats::json::Value>);

}  // namespace tour_guide