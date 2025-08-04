package io.github.sakurawald.fuji.module.initializer.command_rewrite.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.structure.RegexRewriteNode;

import java.util.ArrayList;
import java.util.List;

public class CommandRewriteConfigModel {

    @Document(id = 1751826280914L, value = """
        Defined `rewrite` entries.
        """)
    @SerializedName(value = "rules", alternate = {"regex", "rewrite"})
    public List<RegexRewriteNode> rules = new ArrayList<>() {
        {
            this.add(new RegexRewriteNode("home", "home tp default"));
        }
    };
}
