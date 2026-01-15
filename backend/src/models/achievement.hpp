#pragma once

#include <string>
#include <vector>

#include <userver/formats/json_fwd.hpp>

namespace tour_guide {

struct Achievement {
    int id;
    int type;
    int count;
    std::vector<std::string> tag;
};

userver::formats::json::Value Serialize(const Achievement& achievement, userver::formats::serialize::To<userver::formats::json::Value>);

}  // namespace tour_guide