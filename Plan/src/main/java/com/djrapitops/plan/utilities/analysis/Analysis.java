package main.java.com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.data.analysis.*;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.PageCache;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webserver.response.AnalysisPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.PlayersPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.api.JsonResponse;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class Analysis {

    private final Plan plugin;
    private int taskId = -1;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public Analysis(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Analyzes the data of all offline players on the server.
     *
     * @param infoManager InformationManager of the plugin.
     */
    public void runAnalysis(InformationManager infoManager) {
        if (isAnalysisBeingRun()) {
            return;
        }

        Benchmark.start("Analysis");
        log(Locale.get(Msg.ANALYSIS_START).toString());
        // Async task for Analysis
        plugin.getRunnableFactory().createNew(new AbsRunnable("AnalysisTask") {
            @Override
            public void run() {
                taskId = this.getTaskId();
                analyze(infoManager, plugin.getDB());
                taskId = -1;
                this.cancel();
            }
        }).runTaskAsynchronously();
    }

    /**
     * Caches analyzed data of db to the provided cache analysisCache.
     *
     * @param infoManager InformationManager of the plugin.
     *                      method.
     * @param db            Database which data will be analyzed.
     * @return Whether or not analysis was successful.
     */
    public boolean analyze(InformationManager infoManager, Database db) {
        log(Locale.get(Msg.ANALYSIS_FETCH).toString());
        Benchmark.start("Fetch Phase");
        Log.debug("Database", "Analysis Fetch");
        Log.debug("Analysis", "Analysis Fetch Phase");
        //TODO Rewrite FETCH
        List<TPS> tpsData = new ArrayList<>();

        try {
            tpsData = db.getTpsTable().getTPSData();
            Log.debug("Analysis", "TPS Data Size: " + tpsData.size());
        } catch (Exception ex) {
            Log.toLog(this.getClass().getName(), ex);
        }

        return analyzeData(tpsData, infoManager);
    }

    /**
     * @param tpsData
     * @param infoManager InformationManager of the plugin.
     * @return
     */
    public boolean analyzeData(List<TPS> tpsData, InformationManager infoManager) {
        try {
//            rawData.sort(new UserDataLastPlayedComparator());
//            List<UUID> uuids = rawData.stream().map(UserInfo::getUuid).collect(Collectors.toList());
            Benchmark.start("Create Empty dataset");
            DataCache dataCache = plugin.getDataCache();
            Map<String, Integer> commandUse = plugin.getDB().getCommandUse();

            AnalysisData analysisData = new AnalysisData(commandUse, tpsData);
            analysisData.setPluginsTabLayout(plugin.getHookHandler().getPluginsTabLayoutForAnalysis());
            analysisData.setPlanVersion(plugin.getVersion());
            ActivityPart activityPart = analysisData.getActivityPart();
            // TODO GetRecentPlayers
//            activityPart.setRecentPlayersUUIDs(uuids);
//            analysisData.getPlayerCountPart().addPlayers(uuids);

//            activityPart.setRecentPlayers(rawData.stream().map(UserInfo::getName).collect(Collectors.toList()));

            Benchmark.stop("Analysis", "Create Empty dataset");
            long fetchPhaseLength = Benchmark.stop("Analysis", "Fetch Phase");

            Benchmark.start("Analysis Phase");
            Log.debug("Analysis", "Analysis Phase");

            //TODO Fetch Size
            log(Locale.get(Msg.ANALYSIS_PHASE_START).parse(0, fetchPhaseLength));

            // TODO Create playersTable
//            String playersTable = PlayersTableCreator.createSortablePlayersTable(rawData);
//            analysisData.setPlayersTable(playersTable);

            fillDataset(analysisData);
            // Analyze
            analysisData.analyseData();
            Benchmark.stop("Analysis", "Analysis Phase");

            log(Locale.get(Msg.ANALYSIS_3RD_PARTY).toString());
            Log.debug("Analysis", "Analyzing additional data sources (3rd party)");
//    TODO        analysisData.setAdditionalDataReplaceMap(analyzeAdditionalPluginData(uuids));

            infoManager.cacheAnalysisdata(analysisData);
            long time = Benchmark.stop("Analysis", "Analysis");

            Log.logDebug("Analysis", time);

            Log.info(Locale.get(Msg.ANALYSIS_FINISHED).parse(String.valueOf(time), HtmlUtils.getServerAnalysisUrlWithProtocol()));

            PageCache.removeIf(identifier -> identifier.startsWith("inspectPage: ") || identifier.startsWith("inspectionJson: "));
            PageCache.cachePage("analysisPage", () -> new AnalysisPageResponse(plugin.getInfoManager()));
            PageCache.cachePage("analysisJson", () -> new JsonResponse(analysisData));
            PageCache.cachePage("players", () -> new PlayersPageResponse(plugin));

            // TODO Export
//            ExportUtility.export(analysisData, rawData);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            Log.debug("Analysis", "Error: " + e);
            Log.logDebug("Analysis");
            return false;
        }
        return true;
    }

    private void log(String msg) {
        // TODO Send info to the command sender. (Needs a new system)
        Log.info(msg);
    }

    private Map<String, Serializable> analyzeAdditionalPluginData(List<UUID> uuids) {
        // TODO Rewrite
        Benchmark.start("3rd party");
        final Map<String, Serializable> replaceMap = new HashMap<>();
        final HookHandler hookHandler = plugin.getHookHandler();
        final List<PluginData> sources = hookHandler.getAdditionalDataSources().stream()
                .filter(p -> !p.isBanData())
                .filter(p -> !p.getAnalysisTypes().isEmpty())
                .collect(Collectors.toList());
        final AnalysisType[] totalTypes = new AnalysisType[]{
                AnalysisType.INT_TOTAL, AnalysisType.LONG_TOTAL, AnalysisType.LONG_TIME_MS_TOTAL, AnalysisType.DOUBLE_TOTAL
        };
        final AnalysisType[] avgTypes = new AnalysisType[]{
                AnalysisType.INT_AVG, AnalysisType.LONG_AVG, AnalysisType.LONG_TIME_MS_AVG, AnalysisType.LONG_EPOCH_MS_MINUS_NOW_AVG, AnalysisType.DOUBLE_AVG
        };
        final AnalysisType bool = AnalysisType.BOOLEAN_PERCENTAGE;
        final AnalysisType boolTot = AnalysisType.BOOLEAN_TOTAL;
        Log.debug("Analysis", "Additional Sources: " + sources.size());
        sources.parallelStream().filter(Verify::notNull).forEach(source -> {
            try {
                Benchmark.start("Source " + source.getPlaceholder("").replace("%", ""));
                final List<AnalysisType> analysisTypes = source.getAnalysisTypes();
                if (analysisTypes.isEmpty()) {
                    return;
                }
                if (analysisTypes.contains(AnalysisType.HTML)) {
                    replaceMap.put(source.getPlaceholder(AnalysisType.HTML.getPlaceholderModifier()), source.getHtmlReplaceValue(AnalysisType.HTML.getModifier(), uuids.get(0)));
                    return;
                }
                for (AnalysisType type : totalTypes) {
                    if (analysisTypes.contains(type)) {
                        replaceMap.put(source.getPlaceholder(type.getPlaceholderModifier()), AnalysisUtils.getTotal(type, source, uuids));
                    }
                }
                for (AnalysisType type : avgTypes) {
                    if (analysisTypes.contains(type)) {
                        replaceMap.put(source.getPlaceholder(type.getPlaceholderModifier()), AnalysisUtils.getAverage(type, source, uuids));
                    }
                }
                if (analysisTypes.contains(bool)) {
                    replaceMap.put(source.getPlaceholder(bool.getPlaceholderModifier()), AnalysisUtils.getBooleanPercentage(bool, source, uuids));
                }
                if (analysisTypes.contains(boolTot)) {
                    replaceMap.put(source.getPlaceholder(boolTot.getPlaceholderModifier()), AnalysisUtils.getBooleanTotal(boolTot, source, uuids));
                }
            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
                Log.error("A PluginData-source caused an exception: " + source.getPlaceholder("").replace("%", ""));

                Log.toLog(this.getClass().getName(), e);
            } finally {
                Benchmark.stop("Analysis", "Source " + source.getPlaceholder("").replace("%", ""));
            }
        });
        Benchmark.stop("Analysis", "3rd party");
        return replaceMap;
    }

    /**
     * @return
     */
    public boolean isAnalysisBeingRun() {
        return taskId != -1;
    }

    public void setTaskId(int id) {
        taskId = id;
    }

    private void fillDataset(AnalysisData analysisData) {
        ActivityPart activity = analysisData.getActivityPart();
        GamemodePart gmPart = analysisData.getGamemodePart();
        GeolocationPart geolocPart = analysisData.getGeolocationPart();
        JoinInfoPart joinInfo = analysisData.getJoinInfoPart();
        KillPart killPart = analysisData.getKillPart();
        PlayerCountPart playerCount = analysisData.getPlayerCountPart();
        PlaytimePart playtime = analysisData.getPlaytimePart();
        WorldPart worldPart = analysisData.getWorldPart();

        long now = MiscUtils.getTime();

        Benchmark.start("Fill Dataset");
        List<PluginData> banSources = plugin.getHookHandler().getAdditionalDataSources()
                .stream().filter(PluginData::isBanData).collect(Collectors.toList());
//        rawData.forEach(uData -> {
//        TODO    Map<String, Long> worldTimes = uData.getWorldTimes().getTimes();

        // TODO     playtime.addToPlaytime(playTime);
//            joinInfo.addToLoginTimes(uData.getLoginTimes());
//            joinInfo.addRegistered(uData.getRegistered());

        //TODO      geolocPart.addGeolocation(uData.getGeolocation());

//            final UUID uuid = uData.getUuid();
//            if (uData.isOp()) {
//                playerCount.addOP(uuid);
//            }

//            boolean banned = uData.isBanned();
//            if (!banned) {
//                banned = banSources.stream()
//                        .anyMatch(banData -> {
//                            Serializable value = banData.getValue(uuid);
//                            if (value instanceof Boolean) {
//                                return (Boolean) value;
//                            }
//                            return false;
//                        });
//            }
//
//            if (banned) {
//                activity.addBan(uuid);
//            } else if (uData.getLoginTimes() == 1) {
//                activity.addJoinedOnce(uuid);
//        TODO    } else if (AnalysisUtils.isActive(now, uData.getLastPlayed(), playTime, uData.getSessionCount())) {
//                activity.addActive(uuid);
//            } else {
//                activity.addInActive(uuid);
//            }
        //TODO    List<PlayerKill> playerKills = uData.getPlayerKills();

//            List<Session> sessions = uData.getSessions();
//            joinInfo.addSessions(uuid, sessions);
//        });
        Benchmark.stop("Analysis", "Fill Dataset");
    }
}
