#include "user.hpp"

#include <userver/formats/json/value_builder.hpp>

namespace tour_guide {

userver::formats::json::Value
Serialize(const User& user, userver::formats::serialize::To<userver::formats::json::Value>) {
    userver::formats::json::ValueBuilder builder;
    builder["id"] = user.id;
    builder["login"] = user.login;
    builder["password"] = user.password;

    return builder.ExtractValue();
}

}  //  namespace tour_guide
