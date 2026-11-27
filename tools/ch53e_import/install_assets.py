import json, os, shutil

root = r'E:\Dragon_Rise\Dragon_Rise_1211'
assets = os.path.join(root, 'src', 'main', 'resources', 'assets', 'dragonrise_reforge')
data = os.path.join(root, 'src', 'main', 'resources', 'data', 'dragonrise_reforge')

# 1) 模型 + 贴图
shutil.copy(r'E:\Dragon_Rise\_ch53e\ch53e_parts.geo.json',
            os.path.join(assets, 'models', 'bedrock', 'vehicle', 'ch53e.geo.json'))
shutil.copy(r'E:\Dragon_Rise\_ch53e\ch53e.png',
            os.path.join(assets, 'textures', 'entity', 'ch53e.png'))
print('模型/贴图已复制')

model = {"Model": "dragonrise_reforge:models/bedrock/vehicle/ch53e.geo.json",
         "Texture": "dragonrise_reforge:textures/entity/ch53e.png"}

# 2) data 侧完整载具数据（以 uh60 为模板，整体放大约 1.5 倍）
src = json.load(open(os.path.join(data, 'sbw', 'vehicles', 'uh60.json'), encoding='utf-8'))
d = json.loads(json.dumps(src))
K = 1.5


def scale_vec(v, k=K):
    return [round(x * k, 5) for x in v]


d['ID'] = 'dragonrise_reforge:ch53e'
d['MaxHealth'] = 420
d['Mass'] = 14.0
d['Models'] = [model]
d['VehicleIcon'] = 'dragonrise_reforge:textures/vehicle_icon/ch53e_icon.png'
for obb in d.get('OBB', []):
    obb['Size'] = scale_vec(obb['Size'])
    obb['Position'] = scale_vec(obb['Position'])
for seat in d.get('Seats', []):
    if 'Position' in seat:
        seat['Position'] = scale_vec(seat['Position'])
    cam = seat.get('CameraPos') or {}
    if 'AircraftCameraPos' in cam:
        cam['AircraftCameraPos'] = scale_vec(cam['AircraftCameraPos'])
    dis = seat.get('DismountInfo') or {}
    if 'Position' in dis:
        dis['Position'] = scale_vec(dis['Position'])
ei = d.get('EngineInfo') or {}
ei.update({'Speed': 0.85, 'LiftSpeed': 1.0, 'PitchSpeed': 0.6,
           'YawSpeed': 0.7, 'RollSpeed': 0.5, 'EnergyCostRate': 380})
d['EngineInfo'] = ei
if 'ThirdPersonCameraPos' in d:
    d['ThirdPersonCameraPos'] = scale_vec(d['ThirdPersonCameraPos'])
json.dump(d, open(os.path.join(data, 'sbw', 'vehicles', 'ch53e.json'), 'w', encoding='utf-8'),
          ensure_ascii=False, indent=2)
print('data 载具数据已写入')

# 3) assets 侧：模型覆盖（superbwarfare 命名空间）+ 本模组命名空间
for ns_path in [os.path.join(root, 'src', 'main', 'resources', 'assets', 'superbwarfare', 'sbw', 'vehicles', 'ch53e.json'),
                os.path.join(assets, 'sbw', 'vehicles', 'ch53e.json')]:
    os.makedirs(os.path.dirname(ns_path), exist_ok=True)
    json.dump({'ID': 'dragonrise_reforge:ch53e', 'Model': model,
               'MouseSpeed': [0.3, 0.3], 'Models': [model]},
              open(ns_path, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
    print('写入', ns_path.replace(root, '.'))
