# -*- coding: utf-8 -*-
"""导出 Codex 会话 JSONL 为可读 Markdown。"""
import json
import re
import sys
from datetime import datetime, timezone, timedelta

SRC = r"C:\Users\Administrator\.codex\sessions\2026\08\15\rollout-2026-08-15T08-57-51-01a002ec-d976-76f3-9809-97a3c608de05.jsonl"
OUT = r"D:\minecraft\modp\GScode\.codex\session-export\会话导出-2026-08-17.md"

CN_TZ = timezone(timedelta(hours=8))


def fmt_ts(ts):
    try:
        dt = datetime.fromisoformat(ts.replace("Z", "+00:00"))
        return dt.astimezone(CN_TZ).strftime("%Y-%m-%d %H:%M:%S")
    except Exception:
        return ts or ""


def md_text(t):
    t = t.replace("```", "``")
    t = t.replace("\r\n", "\n")
    return t


def truncate(s, n, suffix="\n…（已截断）"):
    return s if len(s) <= n else s[:n] + suffix


def read_records(path):
    recs = []
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                recs.append(json.loads(line))
            except Exception:
                pass
    return recs


def main():
    recs = read_records(SRC)

    # 找到最后一次用户“导出当前会话”的记录下标，导出到此为止
    cut_index = len(recs) - 1
    for i in range(len(recs) - 1, -1, -1):
        r = recs[i]
        if r.get("type") == "event_msg" and r.get("payload", {}).get("type") == "user_message":
            msg = str(r["payload"].get("message", ""))
            if "导出当前会话" in msg:
                cut_index = i
                break

    # 预收集 response_item 消息 id，用于去掉 event_msg 中的重复记录
    seen_ids = set()
    for r in recs:
        if r.get("type") == "response_item" and r.get("payload", {}).get("type") == "message":
            mid = r["payload"].get("id")
            if mid:
                seen_ids.add(mid)

    lines = []
    lines.append("# 当前会话导出\n")
    lines.append("- 导出时间：2026-08-17")
    lines.append("- 会话 ID：01a002ec-d976-76f3-9809-97a3c608de05")
    lines.append("- 会话文件：rollout-2026-08-15T08-57-51-01a002ec-d976-76f3-9809-97a3c608de05.jsonl")
    lines.append("- 工作目录：D:\\minecraft\\modp\\GScode")
    lines.append("- 内容截至：用户消息「导出当前会话到桌面」\n")

    exported_ids = set()
    turn = 0
    user_count = 0
    assist_count = 0

    for r in recs[: cut_index + 1]:
        rtype = r.get("type")
        payload = r.get("payload", {})
        ts = r.get("timestamp", "")

        if rtype == "event_msg":
            ptype = payload.get("type")
            if ptype == "task_started":
                turn += 1
                lines.append("\n---\n")
                lines.append(f"## 第 {turn} 轮（{fmt_ts(ts)}）\n")
            elif ptype == "user_message":
                msg = str(payload.get("message", ""))
                if "<environment_context>" in msg or "<app-context>" in msg:
                    continue
                if payload.get("id") in seen_ids:
                    continue
                user_count += 1
                lines.append(f"### 👤 用户（{fmt_ts(ts)}）\n")
                lines.append(md_text(msg) + "\n")
            elif ptype == "agent_message":
                if payload.get("id") in seen_ids:
                    continue
                assist_count += 1
                lines.append(f"### 🤖 Codex（{fmt_ts(ts)}）\n")
                lines.append(md_text(str(payload.get("message", ""))) + "\n")
            elif ptype in ("context_compacted", "compacted"):
                lines.append(f"> ⚠️ 上下文压缩事件（{fmt_ts(ts)}）：历史内容被总结，之后的对话基于总结继续。\n")

        elif rtype == "response_item":
            ptype = payload.get("type")
            if ptype == "message":
                mid = payload.get("id")
                if mid:
                    if mid in exported_ids:
                        continue
                    exported_ids.add(mid)
                role = payload.get("role")
                texts = []
                for c in payload.get("content", []):
                    if role == "assistant" and c.get("type") == "output_text":
                        texts.append(c.get("text", ""))
                    elif role == "assistant" and c.get("type") == "refusal":
                        texts.append("（拒绝）" + c.get("text", ""))
                    elif role == "user" and c.get("type") == "input_text":
                        t = c.get("text", "")
                        if "<environment_context>" in t or "<app-context>" in t:
                            continue
                        if re.match(r"^\s*<(skills_instructions|permissions instructions|collaboration_mode|multi_agent_mode|apps_instructions)>", t):
                            continue
                        texts.append(t)
                joined = "\n\n".join(t for t in texts if t.strip())
                if joined.strip():
                    if role == "user":
                        user_count += 1
                        lines.append(f"### 👤 用户（{fmt_ts(ts)}）\n")
                    else:
                        assist_count += 1
                        lines.append(f"### 🤖 Codex（{fmt_ts(ts)}）\n")
                    lines.append(md_text(joined) + "\n")
            elif ptype == "function_call":
                name = payload.get("name", "?")
                args = payload.get("arguments", "")
                try:
                    args_pretty = json.dumps(json.loads(args), ensure_ascii=False, indent=2)
                except Exception:
                    args_pretty = str(args)
                lines.append(f"<details><summary>🔧 工具调用：{name}</summary>\n")
                lines.append("```")
                lines.append(truncate(args_pretty, 2500))
                lines.append("```")
                lines.append("</details>\n")
            elif ptype == "function_call_output":
                out_text = str(payload.get("output", ""))
                lines.append("<details><summary>📤 工具输出（截断至 4000 字符）</summary>\n")
                lines.append("```")
                lines.append(md_text(truncate(out_text, 4000)))
                lines.append("```")
                lines.append("</details>\n")

    content = "\n".join(lines)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write(content)

    print(f"written: {OUT}")
    print(f"size: {len(content.encode('utf-8'))} bytes")
    print(f"turns={turn} users={user_count} assistants={assist_count}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
