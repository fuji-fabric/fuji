package io.github.sakurawald.core.config.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/* sometimes i really hate the typing system of java... */
public class ConfigModel {

    public Core core = new Core();
    public Modules modules = new Modules();

    public static class Core {

        public Debug debug = new Debug();
        public Backup backup = new Backup();
        public Language language = new Language();
        public Permission permission = new Permission();
        public Quartz quartz = new Quartz();

        public static class Quartz {
            public String logger_level = "WARN";
        }

        public static class Backup {

            public int max_slots = 15;
            public List<String> skip = new ArrayList<>() {
                {
                    this.add("modules/head");
                }
            };
        }

        public static class Language {
            public String default_language = "en_US";
        }


        public static class Permission {
            public boolean all_commands_require_level_4_permission_to_use_by_default = false;
        }

        public static class Debug {
            public boolean disable_all_modules = false;
            public boolean log_debug_messages = false;
        }
    }

    @SuppressWarnings("unused")
    public static class Modules {
        @SerializedName(value = "fuji", alternate = "config")
        public Fuji fuji = new Fuji();
        public Language language = new Language();
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
        public SystemMessage system_message = new SystemMessage();
        public Cleaner cleaner = new Cleaner();
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
        public World world = new World();
        public TeleportWarmup teleport_warmup = new TeleportWarmup();
        public TopChunks top_chunks = new TopChunks();
        public Skin skin = new Skin();
        public WorldDownloader world_downloader = new WorldDownloader();
        public Whitelist whitelist = new Whitelist();
        public Head head = new Head();
        public Profiler profiler = new Profiler();
        public Tester tester = new Tester();
        public Multiplier multiplier = new Multiplier();
        public Disabler disabler = new Disabler();
        public AntiBuild anti_build = new AntiBuild();
        public Color color = new Color();
        public Kit kit = new Kit();
        public TempBan temp_ban = new TempBan();
        public CommandMeta command_meta = new CommandMeta();
        public Gameplay gameplay = new Gameplay();

        public static class World {
            public boolean enable = false;
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
            public Rewrite rewrite = new Rewrite();
            public Replace replace = new Replace();
            public Stripe stripe = new Stripe();
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

            public static class Rewrite {
                public boolean enable = true;
            }

            public static class Stripe {
                public boolean enable = false;
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
            public boolean enable = false;

            public SendMessage send_message = new SendMessage();
            public SendBroadcast send_broadcast = new SendBroadcast();
            public SendActionBar send_actionbar = new SendActionBar();
            public SendTitle send_title = new SendTitle();
            public SendToast send_toast = new SendToast();
            public SendChat send_chat = new SendChat();
            public SendBossbar send_bossbar = new SendBossbar();
            public SendCustom send_custom = new SendCustom();

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
            // the only module to enable by default
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
            public Anvil anvil = new Anvil();

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
            public Anvil anvil = new Anvil();
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
            public Chain chain = new Chain();
            public Delay delay = new Delay();
            public Json json = new Json();
            public Attachment attachment = new Attachment();
            public Shell shell = new Shell();

            public static class Run {
                public boolean enable = false;
            }

            public static class ForEach {
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

        }

        public static class CommandToolbox {
            public boolean enable = false;
            public Bed bed = new Bed();
            public Extinguish extinguish = new Extinguish();
            public Feed feed = new Feed();
            public Fly fly = new Fly();
            public God god = new God();
            public Hat hat = new Hat();
            public Sit sit = new Sit();
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
            public Warp warp = new Warp();
            public Burn burn = new Burn();
            public HelpOp help_op = new HelpOp();
            public Near near = new Near();
            public Jump jump = new Jump();
            public Compass compass = new Compass();
            public Glow glow = new Glow();
            public Freeze freeze = new Freeze();

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

            public static class Sit {
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
        }

        public static class CommandDebug {
            public boolean enable = false;
        }

        public static class CommandAdvice {
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

    }

}
