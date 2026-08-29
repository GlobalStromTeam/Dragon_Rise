package com.redabysslucia.dragonrise_reforge.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Mesh 模型修改器：删除模型文件中所有 "normals"（顶点法线）字段。
 * <p>
 * 处理 {@code assets/dragonrise_reforge/models/bedrock/vehicle/} 与 {@code assets/dragonrise_reforge/geo/}
 * 下的 {@code *.geo.json}。使用文本级方括号配对删除，只移除 normals 字段，
 * 保留文件其余格式（缩进/字段顺序）不变，幂等可重复运行。
 */
public class MeshModelNormalRemover implements DataProvider {

    private final PackOutput output;
    private final ExistingFileHelper existingFileHelper;

    public MeshModelNormalRemover(PackOutput output, ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return CompletableFuture.runAsync(() -> {
            try {
                String workingDir = System.getProperty("user.dir").replace("\\run-data", "").replace("/run-data", "");
                List<Path> dirs = new ArrayList<>();
                Path bedrock = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/models/bedrock/vehicle");
                Path geo = Path.of(workingDir).resolve("src/main/resources/assets/dragonrise_reforge/geo");
                if (Files.exists(bedrock)) dirs.add(bedrock);
                if (Files.exists(geo)) dirs.add(geo);

                int cleaned = 0;
                for (Path dir : dirs) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.geo.json")) {
                        for (Path file : stream) {
                            String content = Files.readString(file);
                            String modified = removeNormals(content);
                            if (!modified.equals(content)) {
                                Files.writeString(file, modified);
                                cleaned++;
                                System.out.println("MeshModelNormalRemover: removed normals from " + dir.getFileName() + "/" + file.getFileName());
                            }
                        }
                    }
                }
                System.out.println("MeshModelNormalRemover: cleaned " + cleaned + " model file(s)");
            } catch (Exception e) {
                throw new RuntimeException("Failed to remove normals from mesh models", e);
            }
        });
    }

    /**
     * 文本级删除所有 {@code "normals": [...]} 字段（方括号配对，支持嵌套数组）。
     * 自动处理字段前后的逗号，保证删除后 JSON 依然合法。
     */
    static String removeNormals(String content) {
        StringBuilder sb = new StringBuilder(content.length());
        int searchFrom = 0;
        while (true) {
            int i = content.indexOf("\"normals\"", searchFrom);
            if (i < 0) break;

            int colon = content.indexOf(':', i);
            if (colon < 0) break;
            int bracket = content.indexOf('[', colon);
            if (bracket < 0) break;

            // 方括号配对（normals 值为嵌套数组 [[x,y,z], ...]）
            int depth = 0;
            int j = bracket;
            for (; j < content.length(); j++) {
                char c = content.charAt(j);
                if (c == '[') {
                    depth++;
                } else if (c == ']') {
                    depth--;
                    if (depth == 0) break;
                }
            }
            if (depth != 0 || j >= content.length()) {
                break; // 未配对的异常结构，停止处理
            }
            int end = j + 1;

            // 决定删除范围：优先连同字段前的逗号一起删，否则删字段后的逗号
            int delStart = i;
            int delEnd = end;
            int t = i - 1;
            while (t >= 0 && Character.isWhitespace(content.charAt(t))) {
                t--;
            }
            if (t >= 0 && content.charAt(t) == ',') {
                delStart = t;
            } else {
                t = end;
                while (t < content.length() && Character.isWhitespace(content.charAt(t))) {
                    t++;
                }
                if (t < content.length() && content.charAt(t) == ',') {
                    delEnd = t + 1;
                }
            }

            sb.append(content, searchFrom, delStart);
            searchFrom = delEnd;
        }
        sb.append(content, searchFrom, content.length());
        return sb.toString();
    }

    @Override
    public String getName() {
        return "DragonRise Mesh Model Normal Remover";
    }
}
