def test_get_sight_ok(client):
    resp = client.get("http://localhost:8000/sights/1")

    assert resp.status_code == 200
    data = resp.json()
    assert data["id"] == 1
    assert data["img"] == "img.png"


def test_get_sight_not_found(client):
    resp = client.get("http://localhost:8000/sights/9999")
    assert resp.status_code == 404