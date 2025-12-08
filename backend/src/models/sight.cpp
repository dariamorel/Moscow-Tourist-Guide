#include "sight.hpp"

#include <userver/formats/json/value_builder.hpp>

namespace tour_guide {

userver::formats::json::Value
Serialize(const Sight& sight, userver::formats::serialize::To<userver::formats::json::Value>) {
    userver::formats::json::ValueBuilder builder;
    builder["id"] = sight.id;
    builder["img"] = sight.img;
    builder["lat"] = sight.img;
    builder["lon"] = sight.img;
    builder["tag"] = sight.img;
    builder["short_desc"] = sight.img;
    builder["desc"] = sight.img;
    builder["timetable"] = sight.img;
    builder["addr"] = sight.img;
    builder["phone"] = sight.img;
    builder["subway"] = sight.img;

    return builder.ExtractValue();
}

}  //  namespace tour_guide
