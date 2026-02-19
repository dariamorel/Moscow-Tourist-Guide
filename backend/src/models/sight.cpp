#include "sight.hpp"

#include <userver/formats/json/value_builder.hpp>

namespace tour_guide {

userver::formats::json::Value
Serialize(const Sight& sight, userver::formats::serialize::To<userver::formats::json::Value>) {
    userver::formats::json::ValueBuilder builder;
    builder["id"] = sight.id;
    builder["img"] = sight.img;
    builder["lat"] = sight.lat;
    builder["lon"] = sight.lon;

    userver::formats::json::ValueBuilder tag_builder;
    for (auto t : sight.tag) {
        tag_builder.PushBack(t);
    }
    builder["tag"] = tag_builder;

    builder["short_desc"] = sight.short_desc;
    builder["desc"] = sight.desc;
    builder["timetable"] = sight.timetable;
    builder["addr"] = sight.addr;
    builder["phone"] = sight.phone;
    builder["subway"] = sight.subway;

    return builder.ExtractValue();
}

}  //  namespace tour_guide
