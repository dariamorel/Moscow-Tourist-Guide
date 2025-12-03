import json

with open('../../sightsData/sights_list.json', encoding='utf-8') as f:
    data = json.load(f)

for sight in data['results']:
    img_url = sight['images'][0]['image'] if sight.get('images') else ''
    
    lat = sight['coords']['lat'] if sight.get('coords') else None
    lon = sight['coords']['lon'] if sight.get('coords') else None

    tags = json.dumps(sight.get('categories', []), ensure_ascii=False)

    short_desc = sight.get('description', '').replace("'", "''")
    desc = sight.get('body_text', '').replace("'", "''")

    timetable = sight.get('timetable', '').replace("'", "''")
    addr = sight.get('address', '').replace("'", "''")
    phone = sight.get('phone', '').replace("'", "''")
    subway = sight.get('subway', '').replace("'", "''")
    
    sql = f"""
    INSERT INTO tour_guide.sights (id, img, lat, lon, tag, short_desc, desc, timetable, addr, phone, subway)
    VALUES ({sight['id']}, '{img_url}', {lat}, {lon}, '{tags}', '{short_desc}', '{desc}', '{timetable}', '{addr}', '{phone}', '{subway}');
    """