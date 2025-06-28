package io.github.sakurawald.fuji.module.initializer.command_permission.structure;

import net.luckperms.api.util.Tristate;

public enum CommandPermissionTestResult {
    USE_ORIGINAL_REQUIREMENT(Tristate.UNDEFINED)
    , ALLOW_TO_USE_THE_COMMAND(Tristate.TRUE)
    , DENY_TO_USE_THE_COMMAND(Tristate.FALSE);

    private final Tristate tristate;

    CommandPermissionTestResult(Tristate tristate) {
        this.tristate = tristate;
    }

    public Tristate toTriState() {
        return this.tristate;
    }

}

