package com.example.touristguide.data.repository

import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.domain.interfaces.Repository
import org.osmdroid.util.GeoPoint

class TestRepository: Repository {
    override suspend fun getAllSights(): List<Sight> {
        return listOf(
            Sight(
                id = 34245,
                title = "клуб виртуальной реальности «Нереальное место»",
                address = "Нижний Сусальный пер., д. 5, стр. 5А",
                timetable = "пн–пт 12:00–0:00, сб, вс 10:00–0:00",
                phone = "+7 495 121-33-62",
                description = "Десятки VR-игр всех жанров для всех возрастов, площадка для праздников и нереальная атмосфера киберпанка — всё это ждёт гостей одного из крупнейших VR-клубов Москвы.",
                bodyText = "Согласно Tripadvisor, с 2016 года клуб не покидает топ-10 лучших развлечений столицы. За время существования клуба в нём побывало более 100 000 человек из разных стран мира. \nВ клубе можно классно отдохнуть с друзьями, прийти с детьми, устроить свидание, а также отметить день рождения, провести корпоратив или детский праздник.",
                url = "https://www.nerealnoemesto.ru/",
                coords = GeoPoint(55.75981199999999, 37.66422499999958),
                subway = "",
                images = listOf("https://media.kudago.com/images/place/67/5f/675f550a4878b642807120675afa1646.jpg"),
                categories = listOf("amusement", "kids", "sights")
            )
        )
    }
}