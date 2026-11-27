# -*- coding: utf-8 -*-
"""填充 BM-13N 喀秋莎的语言键（避免 PowerShell here-string 的引号问题）。"""
import io
import os

ZH = {
    'entity.dragonrise_reforge.bm13n': 'BM-13N 喀秋莎火箭炮',
    'info.dragonrise_reforge.bm13n': (
        'BM-13N 是苏联二战时期著名的多管火箭炮，安装在美援 Studebaker US6 卡车底盘上，'
        '发射架可抬起 16 发 132mm M-13 火箭弹。一次齐射十余秒即可覆盖大片区域，'
        '密集的尾焰与啸叫让它获得了「喀秋莎」的昵称，也成了德军口中的「斯大林管风琴」。'
    ),
    'weapon.dragonrise_reforge:bm13n_rocket': '132mm M-13 火箭弹',
}

EN = {
    'entity.dragonrise_reforge.bm13n': 'BM-13N Katyusha',
    'info.dragonrise_reforge.bm13n': (
        'The BM-13N Katyusha is the famous Soviet multiple rocket launcher of WWII, mounted on a '
        'Lend-Lease Studebaker US6 truck and carrying sixteen rails of 132mm M-13 rockets. A full '
        'salvo covers a wide area in little more than ten seconds; the screaming barrage earned it '
        'the nickname Katyusha and, from German troops, the Stalin organ.'
    ),
    'weapon.dragonrise_reforge:bm13n_rocket': '132mm M-13 Rocket',
}


def fill(path, table):
    with io.open(path, encoding='utf-8') as fp:
        text = fp.read()
    n = 0
    for key, value in table.items():
        old = '"%s": ""' % key
        new = '"%s": "%s"' % (key, value)
        if old in text:
            text = text.replace(old, new)
            n += 1
        else:
            print('  未找到空键:', key)
    with io.open(path, 'w', encoding='utf-8') as fp:
        fp.write(text)
    print('写入', path, n, '条')


if __name__ == '__main__':
    base = os.path.join('src', 'main', 'resources', 'assets', 'dragonrise_reforge', 'lang')
    fill(os.path.join(base, 'zh_cn.json'), ZH)
    fill(os.path.join(base, 'en_us.json'), EN)
