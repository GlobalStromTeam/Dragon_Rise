import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.google.gson.Gson;
import java.io.*;
public class AnimParseTest {
    public static void main(String[] args) throws Exception {
        Gson g = new Gson();
        BedrockAnimationFile f = g.fromJson(new FileReader(args[0]), BedrockAnimationFile.class);
        System.out.println("animations: " + (f.getAnimations() == null ? "NULL" : f.getAnimations().keySet()));
        if (f.getAnimations() != null) {
            for (String k : f.getAnimations().keySet()) {
                var a = f.getAnimations().get(k);
                System.out.println("  " + k + " -> bones=" + (a.getBones() == null ? "NULL" : a.getBones().keySet()));
            }
        }
    }
}