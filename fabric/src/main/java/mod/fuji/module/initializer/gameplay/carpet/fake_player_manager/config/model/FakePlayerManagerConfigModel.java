package mod.fuji.module.initializer.gameplay.carpet.fake_player_manager.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import java.util.ArrayList;
import java.util.List;

public class FakePlayerManagerConfigModel {

    @Document(id = 1751977086541L, value = """
        The `rules` to define how many fake-players can each player spawned. (At different times)

        The tuple means (`day_of_week`, `minutes_of_the_day`, `max_fake_players_per_player`)
        The range of `day_of_week` is `[1, 7]`
        The range of `minutes_of_the_day` is `[0, 1440]`

        Taken the tuple `(1, 0 2)` for example.
        The rule says, if `day_of_week` >= 1, and `minutes_of_the_day` >= 0, then the `max_fake_players_per_player` is now `2`.

        You can define multiple `rules`.
        Rules are matched from up to down.
        The first matched rule will be used.
        """)
    @SerializedName(value = "caps_limit_rules", alternate = "caps_limit_rule")
    public List<List<Integer>> caps_limit_rules = new ArrayList<>() {
        {
            this.add(List.of(1, 0, 2));
        }
    };

    @Document(id = 1751977366991L, value = """
        The `renew duration` when using the `/player renew` command.
        """)
    public int renew_duration_ms = 1000 * 60 * 60 * 12;

    @Document(id = 1751977426136L, value = """
        The format of `fake player name`.

        You can use this option to define the `prefix` and `suffix` of `fake player name`.
        """)
    public String transform_name = "_fake_%s";

}
