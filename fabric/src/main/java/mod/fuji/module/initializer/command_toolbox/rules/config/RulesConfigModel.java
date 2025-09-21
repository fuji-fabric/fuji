package mod.fuji.module.initializer.command_toolbox.rules.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RulesConfigModel {

    public String rules = """
        <blue>===== custom text =====
        Hello <orange>%player:name%</orange>, you are in <orange>%world:name%</orange> now.
        <hover:show_text:'you see me!'>Hover me</hover>
        <click:run_command:'/back'>click me to run `/back` command</click>
        <newpage><blue>This is the second page!
        <click:suggest_command:'/back'>click me to suggest /back command (This doesn't work inside a book)</click>
        <insert:'hello'>shift + click me to insert "hello" (This doesn't work inside a book)</insert>
        <click:open_url:'https://placeholders.pb4.eu/user/text-format/'>click me to open the url</click>
        <newpage>This is the third page!
        <bold><click:change_page:'1'>click me to the first page</click></bold>
        <orange>You can press `<keybind:'key.jump'>` key to jump!</orange>
        <gradient:red:green:blue>This is gradient text.</gradient>
        <rb>The rainbow text</rb>
        <newpage>The end.
        """;
}
