#include "view.hpp"

#include "../../../../models/user.hpp"

#include <userver/formats/json.hpp>
#include <userver/storages/postgres/component.hpp>

namespace tour_guide {

RegistrationHandler::RegistrationHandler(
    const userver::components::ComponentConfig& config,
    const userver::components::ComponentContext& component_context
)
    : HttpHandlerBase(config, component_context),
      pg_cluster_(component_context.FindComponent<userver::components::Postgres>("postgres-db-1").GetCluster()) {}

std::string RegistrationHandler::
    HandleRequestThrow(const userver::server::http::HttpRequest& request, userver::server::request::RequestContext&)
        const {
    auto json = userver::formats::json::FromString(request.RequestBody());

    std::string login = json["login"].As<std::string>("");
    std::string password = json["password"].As<std::string>("");

    if (login.empty() || password.empty()) {
        return userver::server::http::HttpResponse{}
            .SetStatus(userver::server::http::HttpStatus::kBadRequest)
            .SetBody("Login or password missing");
    }

    auto row = pg_cluster_->Execute(
        userver::storages::postgres::ClusterHostType::kMaster,
        "INSERT INTO tour_guide.users(login, password) VALUES($1, crypt($2, gen_salt('bf'))) "
        "RRETURNING id ",
        login, password
    );

    if (row.IsEmpty()) {
        return userver::server::http::HttpResponse{}
            .SetStatus(userver::server::http::HttpStatus::kBadRequest)
            .SetBody("Can't create user");
    }

    User user;
    user.id = row["id"].As<int>;
    user.login = login;
    user.password = password;

    return ToString(userver::formats::json::ValueBuilder{user}.ExtractValue());
}

}  // namespace tour_guide
