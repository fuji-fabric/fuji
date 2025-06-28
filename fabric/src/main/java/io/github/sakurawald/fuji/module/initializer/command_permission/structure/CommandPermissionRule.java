package io.github.sakurawald.fuji.module.initializer.command_permission.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

@Document("""
    This object describes one rule.
    """)
public class CommandPermissionRule {
    @Document("""
        The `regex` used to match the `corresponding permission` string.
        """)
    public String permissionPatternRegex;

    @Document("""
        For the `matched permission string`, we directly return the `pre-defined` permission test result.
        """)
    public CommandPermissionTestResult permissionTestResult;
}
