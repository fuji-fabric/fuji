package io.github.sakurawald.fuji.module.initializer.command_permission.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(id = 1751826762371L, value = """
    This object describes one rule.
    """)
public class CommandPermissionRule {
    @Document(id = 1751826763999L, value = """
        The `regex` used to match the `corresponding permission` string.
        """)
    public String permissionPatternRegex;

    @Document(id = 1751826766512L, value = """
        For the `matched permission string`, we directly return the `pre-defined` permission test result.
        """)
    public CommandPermissionTestResult permissionTestResult;
}
