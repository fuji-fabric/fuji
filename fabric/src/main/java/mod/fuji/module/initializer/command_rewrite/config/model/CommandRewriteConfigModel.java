package mod.fuji.module.initializer.command_rewrite.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.structure.RegexRewriteRule;

import java.util.ArrayList;
import java.util.List;

public class CommandRewriteConfigModel {

    @Document(id = 1751826280914L, value = """
        Defined `rewrite` entries.
        """)
    @SerializedName(value = "rules", alternate = {"regex", "rewrite"})
    public List<RegexRewriteRule> rules = new ArrayList<>() {
        {
            this.add(new RegexRewriteRule("\\?", "help"));
        }
    };
}
