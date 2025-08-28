package io.github.sakurawald.fuji.core.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;

@Document(id = 1754987954725L, value = """
    This file is named `Main Control File`.
    It's used to:
    1. Configure the options for `core`, which affects all `modules`.
    2. Enable or disable a `module`.

    <green>NOTE: You need to re-start the server, to `enable` or `disable` a `module`.
    """)
public class MainControlConfigModel {

    @Document(id = 1751823676896L, value = """
        Fuji is composed by `core` and `module`.
        The `core` config affects `all` modules.
        The `module` config only affects that specific module.
        """)
    public Core core = new Core();
    public static class Core {
        @Document(id = 1751823723851L, value = "Debug related options.")
        public Debug debug = new Debug();

        @Document(id = 1751823731455L, value = """
            Fuji will back up the `config/fuji` dir before it loads any module.
            """)
        public Backup backup = new Backup();

        @Document(id = 1751823748647L, value = """
            The language related options.
            """)
        public Core.Language language = new Core.Language();

        @Document(id = 1755571534946L, value = """
            The options for all fuji commands.
            """)
        public Command command = new Command();


        @Document(id = 1751823755527L, value = """
            The permission related options.
            """)
        public Permission permission = new Permission();

        @Document(id = 1751823761222L, value = """
            The scheduler related options.
            The `scheduler` system is used to run `jobs`.
            """)
        public Scheduler scheduler = new Scheduler();

        public static class Scheduler {
            @Document(id = 1751823767950L, value = """
                The logger level for `quartz` library.
                The level is recommended to be higher than `WARN`, to prevent console spam.
                Acceptable levels: ALL < TRACE < DEBUG < INFO < WARN < ERROR < FATAL < OFF
                """)
            public String logger_level = "WARN";
        }

        public Formatter formatter = new Formatter();
        public static class Formatter {
            public String date_formatter = "yyyy-MM-dd HH:mm:ss";
        }

        public DocumentSection document = new DocumentSection();
        public static class DocumentSection {
            @Document(id = 1752484948715L, value = """
                When `enable` this option, we will always use the `built-in doc strings`, which is written in `English`.
                This option is used to ensure you always see the `latest version` of `doc strings` from your `current using version`.

                When `disable` this option, we will ues the `doc strings` from the `language files`.
                """)
            public boolean alwaysUseBuiltInDocStrings = true;
        }


        public static class Backup {

            @Document(id = 1751823774480L, value = """
                How many `backup files` should we keep?
                """)
            public int max_slots = 15;

            @Document(id = 1751823780583L, value = """
                The `paths` that should be skipped when backup.
                The `path` is resolved and related to `config/fuji/` dir.
                """)
            public List<String> skip = new ArrayList<>() {
                {
                    this.add("modules/head/head-data");
                }
            };
        }

        public static class Language {
            @Document(id = 1751823787824L, value = """
                The `default language` used by Fuji.
                The language files are located in `config/fuji/lang` dir.
                """)
            public String default_language = "en_US";

            public Validator validator = new Validator();
            public static class Validator {
                @Document(id = 1754322053339L, value = """
                Should we validate the `arguments` when loading a `language file`?
                """)
                public boolean validate_arguments = true;
            }
        }

        public static class Command {

            @Document(id = 1755571654986L, value = """
                The `command assistant` offers an `auto help` feature for all fuji commands.
                It dynamically inspects possible command paths and provides users with real-time command hints.

                <green>NOTE: To hot-switch this feature without a server re-start, you have to:
                1. Issue `/fuji reload` first, to reload the `main control file`.
                2. Issue `/reload`, to reload `all the commands`.
                """)
            public Assistant assistant = new Assistant();
            public static class Assistant {
                public boolean enable = true;

                @Document(id = 1755571789064L, value = """
                    The requirement to use the `command assistant`.
                    """)
                public Requirement requirement = new Requirement();
                public static class Requirement {
                    public int level_permission = 0;
                }

            }

        }

        public static class Permission {
            @Document(id = 1751823793347L, value = """
                Fuji defines commands into 2 groups, for different users.
                One group for `normal user`, these commands require `level 0 permission` to use.
                One group for `admin user`, these commands require `level 4 permission` to use.

                If you want to define the permission of commands by yourself,
                you can enable `this` option.
                And use `command_permission` module to define permission for each command.
                """)
            public boolean all_commands_require_level_4_permission_to_use_by_default = false;
        }

        public static class Debug {
            @Document(id = 1751823800414L, value = """
                Force disable `all` modules.
                Used to test the compatibility between `fuji` and `other mods`.
                """)
            public boolean disable_all_modules = false;

            @Document(id = 1751823806770L, value = """
                Should we log the `debug` level messages into the `console`?
                This option can be changed using `/fuji debug` command.
                """)
            public boolean log_debug_messages = false;

            @Document(id = 1751823813518L, value = """
                Should we print the user guide in the console on server startup?
                """)
            public boolean print_user_guide_in_console = true;
        }
    }


    @Document(id = 1751823688579L, value = """
        Fuji is designed to be fully-modular.
        All modules is `disabled` by default.
        You can modify the `enable` field to enable a module <red>after a server re-start</red>.
        <b>Remember to `restart` the server to apply the modification.

        Issue `/fuji inspect modules` to see the module status.
        """)
    public Modules modules = new Modules();

    @SuppressWarnings("unused")
    public static class Modules {
        @SerializedName(value = "fuji", alternate = "config")
        public Fuji fuji = new Fuji();
        public Modules.Language language = new Modules.Language();
        public Chat chat = new Chat();
        public Placeholder placeholder = new Placeholder();
        public Predicate predicate = new Predicate();
        public MOTD motd = new MOTD();
        public Nametag nametag = new Nametag();
        public Tab tab = new Tab();
        public Tpa tpa = new Tpa();
        public Back back = new Back();
        public Home home = new Home();
        public Pvp pvp = new Pvp();
        public Afk afk = new Afk();
        public Rtp rtp = new Rtp();
        public Works works = new Works();
        public DeathLog deathlog = new DeathLog();
        public View view = new View();
        public Echo echo = new Echo();
        public Functional functional = new Functional();
        public Economy economy = new Economy();
        public SystemMessage system_message = new SystemMessage();
        public Cleaner cleaner = new Cleaner();
        public World world = new World();
        public Skin skin = new Skin();
        public Title title = new Title();
        public LeaderBoard leaderboard = new LeaderBoard();
        public Jail jail = new Jail();
        public Kit kit = new Kit();
        public Rank rank = new Rank();
        public Head head = new Head();
        public Color color = new Color();
        public Sit sit = new Sit();
        public TeleportWarmup teleport_warmup = new TeleportWarmup();
        public TempBan temp_ban = new TempBan();
        public AntiBuild anti_build = new AntiBuild();
        public Warning warning = new Warning();
        public Maintenance maintenance = new Maintenance();
        public CommandScheduler command_scheduler = new CommandScheduler();
        public CommandPermission command_permission = new CommandPermission();
        public CommandRewrite command_rewrite = new CommandRewrite();
        public CommandAlias command_alias = new CommandAlias();
        public CommandBundle command_bundle = new CommandBundle();
        public CommandAttachment command_attachment = new CommandAttachment();
        public CommandInteractive command_interactive = new CommandInteractive();
        public CommandWarmup command_warmup = new CommandWarmup();
        public CommandCooldown command_cooldown = new CommandCooldown();
        public CommandToolbox command_toolbox = new CommandToolbox();
        public CommandSpy command_spy = new CommandSpy();
        public CommandEvent command_event = new CommandEvent();
        public CommandDebug command_debug = new CommandDebug();
        public CommandAdvice command_advice = new CommandAdvice();
        public CommandMenu command_menu = new CommandMenu();
        public CommandMeta command_meta = new CommandMeta();
        public TopChunks top_chunks = new TopChunks();
        public WorldDownloader world_downloader = new WorldDownloader();
        public Whitelist whitelist = new Whitelist();
        public Profiler profiler = new Profiler();
        public Multiplier multiplier = new Multiplier();
        public Disabler disabler = new Disabler();
        public Launcher launcher = new Launcher();
        public Gameplay gameplay = new Gameplay();
        public Tester tester = new Tester();

        public static class World {
            public boolean enable = false;

            public Border border = new Border();
            public static class Border {
                public boolean enable = true;
            }

            public GameRule gamerule = new GameRule();
            public static class GameRule {
                public boolean enable = true;
            }
        }

        public static class MOTD {
            public boolean enable = false;
        }

        public static class Nametag {
            public boolean enable = false;
        }

        public static class TeleportWarmup {
            public boolean enable = false;

        }

        public static class CommandCooldown {
            public boolean enable = false;

        }

        public static class CommandWarmup {
            public boolean enable = false;

        }

        public static class TopChunks {
            public boolean enable = false;
        }

        public static class Chat {

            public boolean enable = false;

            public Style style = new Style();
            public Display display = new Display();
            public History history = new History();
            public Trigger trigger = new Trigger();
            public Replace replace = new Replace();
            public Mention mention = new Mention();
            public Spy spy = new Spy();

            public static class Style {
                public boolean enable = true;
            }

            public static class History {
                public boolean enable = true;
            }

            public static class Display {
                public boolean enable = true;
            }

            public static class Trigger {
                public boolean enable = true;
            }

            public static class Mention {
                public boolean enable = true;
            }

            public static class Spy {
                public boolean enable = false;
            }

            public static class Replace {
                public boolean enable = true;
            }
        }

        public static class Skin {
            public boolean enable = false;
        }

        public static class Back {
            public boolean enable = false;
        }

        public static class Tpa {
            public boolean enable = false;
        }

        public static class Works {
            public boolean enable = false;
        }

        public static class WorldDownloader {
            public boolean enable = false;
        }

        public static class Disabler {
            public boolean enable = false;

            public ChatSpeedDisabler chat_speed_disabler = new ChatSpeedDisabler();
            public MoveSpeedDisabler move_speed_disabler = new MoveSpeedDisabler();
            public MoveWronglyDisabler move_wrongly_disabler = new MoveWronglyDisabler();
            public MaxPlayerDisabler max_player_disabler = new MaxPlayerDisabler();

            public static class ChatSpeedDisabler {
                public boolean enable = false;
            }

            public static class MoveSpeedDisabler {
                public boolean enable = false;
            }

            public static class MoveWronglyDisabler {
                public boolean enable = false;
            }

            public static class MaxPlayerDisabler {
                public boolean enable = false;
            }
        }

        public static class DeathLog {
            public boolean enable = false;
        }

        public static class Echo {
            public boolean enable = true;

            public SendMessage send_message = new SendMessage();
            public SendBroadcast send_broadcast = new SendBroadcast();
            public SendActionBar send_actionbar = new SendActionBar();
            public SendTitle send_title = new SendTitle();
            public SendToast send_toast = new SendToast();
            public SendChat send_chat = new SendChat();
            public SendBossbar send_bossbar = new SendBossbar();
            public SendCustom send_custom = new SendCustom();
            public SendConsole send_console = new SendConsole();
            public SendDialog send_dialog = new SendDialog();

            public static class SendMessage {
                public boolean enable = true;
            }

            public static class SendBroadcast {
                public boolean enable = true;
            }

            public static class SendActionBar {
                public boolean enable = true;
            }

            public static class SendTitle {
                public boolean enable = true;
            }

            public static class SendToast {
                public boolean enable = true;
            }

            public static class SendChat {
                public boolean enable = true;
            }

            public static class SendBossbar {
                public boolean enable = true;
            }

            public static class SendCustom {
                public boolean enable = true;
            }

            public static class SendConsole {
                public boolean enable = true;
            }

            public static class SendDialog {
                public boolean enable = true;
            }
        }

        public static class View {
            public boolean enable = false;
        }

        public static class Placeholder {
            public boolean enable = false;
        }

        public static class Predicate {
            public boolean enable = false;
        }

        public static class Pvp {
            public boolean enable = false;
        }

        public static class Whitelist {
            public boolean enable = false;
        }

        public static class CommandPermission {
            public boolean enable = false;
        }

        public static class Head {

            public boolean enable = false;
        }

        public static class Profiler {
            public boolean enable = false;
        }

        public static class CommandSpy {
            public boolean enable = false;
        }

        public static class CommandScheduler {
            public boolean enable = false;
        }

        public static class Fuji {
            public boolean enable = true;
        }

        public static class Tester {
            public boolean enable = false;
        }

        public static class Language {
            public boolean enable = false;
        }

        public static class Afk {
            public boolean enable = false;

            public AfkEffect effect = new AfkEffect();

            public static class AfkEffect {
                public boolean enable = true;
            }

        }

        public static class Rtp {
            public boolean enable = false;
        }

        public static class CommandInteractive {
            public boolean enable = false;
        }

        public static class Home {
            public boolean enable = false;
        }

        public static class SystemMessage {
            public boolean enable = false;
        }

        public static class CommandAlias {
            public boolean enable = false;
        }

        public static class CommandBundle {
            public boolean enable = false;
        }

        public static class CommandAttachment {
            public boolean enable = false;
        }

        public static class CommandRewrite {
            public boolean enable = false;
        }

        public static class Multiplier {
            public boolean enable = false;
        }

        public static class AntiBuild {
            public boolean enable = false;

        }

        public static class Color {
            public boolean enable = false;

            public Sign sign = new Sign();
            public Color.Anvil anvil = new Color.Anvil();

            public static class Sign {
                public boolean enable = true;
            }

            public static class Anvil {
                public boolean enable = true;
            }
        }

        public static class Functional {
            public boolean enable = false;

            public Workbench workbench = new Workbench();
            public Enchantment enchantment = new Enchantment();
            public GrindStone grindstone = new GrindStone();
            public StoneCutter stonecutter = new StoneCutter();
            public Functional.Anvil anvil = new Functional.Anvil();
            public Cartography cartography = new Cartography();
            public EnderChest enderchest = new EnderChest();
            public Smithing smithing = new Smithing();
            public Loom loom = new Loom();

            public static class Workbench {
                public boolean enable = false;
            }

            public static class Enchantment {
                public boolean enable = false;
            }

            public static class GrindStone {
                public boolean enable = false;
            }

            public static class StoneCutter {

                public boolean enable = false;
            }

            public static class Anvil {
                public boolean enable = false;
            }

            public static class Cartography {
                public boolean enable = false;
            }

            public static class EnderChest {
                public boolean enable = false;
            }

            public static class Smithing {
                public boolean enable = false;
            }

            public static class Loom {
                public boolean enable = false;
            }

        }

        public static class Gameplay {
            public boolean enable = false;

            public MultiObsidianPlatform multi_obsidian_platform = new MultiObsidianPlatform();
            public Carpet carpet = new Carpet();

            public static class Carpet {
                public boolean enable = false;

                public FakePlayerManager fake_player_manager = new FakePlayerManager();
                public BetterInfo better_info = new BetterInfo();

                public static class FakePlayerManager {
                    public boolean enable = false;
                }

                public static class BetterInfo {
                    public boolean enable = false;
                }
            }

            public static class MultiObsidianPlatform {
                public boolean enable = false;
            }
        }

        public static class CommandMeta {
            public boolean enable = false;

            public Run run = new Run();
            public ForEach for_each = new ForEach();
            public OneOf one_of = new OneOf();
            public Chain chain = new Chain();
            @SerializedName(value = "IF")
            public If IF = new If();
            @SerializedName(value = "NOT")
            public Not NOT = new Not();
            public Nop nop = new Nop();
            public Delay delay = new Delay();
            public Json json = new Json();
            public Attachment attachment = new Attachment();
            public Shell shell = new Shell();
            public WhenOnline when_online = new WhenOnline();

            public static class Run {
                public boolean enable = false;
            }

            public static class ForEach {
                public boolean enable = false;
            }

            public static class OneOf {
                public boolean enable = false;
            }

            public static class Chain {
                public boolean enable = false;
            }

            public static class Delay {
                public boolean enable = false;
            }

            public static class Json {
                public boolean enable = false;
            }

            public static class Attachment {
                public boolean enable = false;
            }

            public static class Shell {
                public boolean enable = false;
            }

            public static class WhenOnline {
                public boolean enable = false;
            }

            public static class If {
                public boolean enable = false;
            }

            public static class Not {
                public boolean enable = false;
            }

            public static class Nop {
                public boolean enable = false;
            }
        }

        public static class Sit {
            public boolean enable = false;
        }

        public static class CommandToolbox {
            public boolean enable = false;
            public Bed bed = new Bed();
            public Extinguish extinguish = new Extinguish();
            public Feed feed = new Feed();
            public Fly fly = new Fly();
            public God god = new God();
            public Hat hat = new Hat();
            public Heal heal = new Heal();
            public Lore lore = new Lore();
            public More more = new More();
            public Ping ping = new Ping();
            public Realname realname = new Realname();
            public Nickname nickname = new Nickname();
            public Repair repair = new Repair();
            public Reply reply = new Reply();
            public Seen seen = new Seen();
            public Suicide suicide = new Suicide();
            public Top top = new Top();
            public TrashCan trashcan = new TrashCan();
            public Tppos tppos = new Tppos();
            public Tphere tphere = new Tphere();
            public Warp warp = new Warp();
            public Burn burn = new Burn();
            public HelpOp help_op = new HelpOp();
            public Near near = new Near();
            public Jump jump = new Jump();
            public Compass compass = new Compass();
            public Glow glow = new Glow();
            public Freeze freeze = new Freeze();
            public Rules rules = new Rules();
            public Speed speed = new Speed();
            public Disconnect disconnect = new Disconnect();
            public ApplyVelocity apply_velocity = new ApplyVelocity();

            public static class Glow {
                public boolean enable = false;
            }

            public static class Freeze {
                public boolean enable = false;
            }

            public static class Bed {
                public boolean enable = false;
            }

            public static class Extinguish {
                public boolean enable = false;
            }

            public static class Feed {
                public boolean enable = false;
            }

            public static class Fly {
                public boolean enable = false;
            }

            public static class God {
                public boolean enable = false;
            }

            public static class Hat {
                public boolean enable = false;
            }

            public static class Heal {
                public boolean enable = false;
            }

            public static class Lore {
                public boolean enable = false;
            }

            public static class More {
                public boolean enable = false;
            }

            public static class Ping {
                public boolean enable = false;
            }

            public static class Realname {
                public boolean enable = false;

            }

            public static class Nickname {
                public boolean enable = false;
            }

            public static class Repair {
                public boolean enable = false;

            }

            public static class Reply {
                public boolean enable = false;
            }

            public static class Seen {
                public boolean enable = false;
            }

            public static class Suicide {
                public boolean enable = false;
            }

            public static class Top {
                public boolean enable = false;
            }

            public static class TrashCan {
                public boolean enable = false;
            }

            public static class Tppos {
                public boolean enable = false;
            }

            public static class Tphere {
                public boolean enable = false;
            }

            public static class Warp {
                public boolean enable = false;
            }

            public static class Burn {
                public boolean enable = false;
            }

            public static class HelpOp {
                public boolean enable = false;
            }

            public static class Near {
                public boolean enable = false;
            }

            public static class Jump {
                public boolean enable = false;
            }

            public static class Compass {
                public boolean enable = false;
            }

            public static class Rules {
                public boolean enable = false;
            }

            public static class Speed {
                public boolean enable = false;
            }

            public static class Disconnect {
                public boolean enable = false;
            }

            public static class ApplyVelocity {
                public boolean enable = false;
            }
        }

        public static class CommandDebug {
            public boolean enable = false;
        }

        public static class CommandAdvice {
            public boolean enable = false;
        }

        public static class CommandMenu {
            public boolean enable = false;
        }

        public static class Tab {
            public boolean enable = false;
        }

        public static class Kit {
            public boolean enable = false;
        }

        public static class TempBan {
            public boolean enable = false;
        }

        public static class CommandEvent {

            public boolean enable = false;

        }

        public static class Cleaner {
            public boolean enable = false;
        }

        public static class Warning {
            public boolean enable = false;
        }

        public static class Economy {
            public boolean enable = false;
        }

        public static class Title {
            public boolean enable = false;
        }

        public static class LeaderBoard {
            public boolean enable = false;
        }

        public static class Jail {
            public boolean enable = false;
        }

        public static class Rank {
            public boolean enable = false;
        }

        public static class Maintenance {
            public boolean enable = false;
        }

        public static class Launcher {
            public boolean enable = false;
        }
    }

}
