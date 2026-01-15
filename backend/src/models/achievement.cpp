#include "achievement.hpp"

#include <userver/formats/json/value_builder.hpp>

namespace tour_guide {

userver::formats::json::Value
Serialize(const Achievement& achievement, userver::formats::serialize::To<userver::formats::json::Value>) {
    userver::formats::json::ValueBuilder builder;
    builder["id"] = achievement.id;
    builder["type"] = achievement.type;
    builder["count"] = achievement.count;
    builder["tag"] = achievement.tag;

    return builder.ExtractValue();
}

}  //  namespace tour_guide
