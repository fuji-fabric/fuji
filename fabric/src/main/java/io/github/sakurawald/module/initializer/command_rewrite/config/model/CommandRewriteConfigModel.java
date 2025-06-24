package io.github.sakurawald.module.initializer.command_rewrite.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.structure.RegexRewriteNode;

import java.util.ArrayList;
import java.util.List;

public class CommandRewriteConfigModel {

    @Document("""
        Defined `rewrite` entries.
        """)
    @SerializedName(value = "rewrite", alternate = "regex")
    public List<RegexRewriteNode> rewrite = new ArrayList<>() {
        {
            this.add(new RegexRewriteNode("home", "home tp default"));
        }
    };
}
