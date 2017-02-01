package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.ServerData;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.PlanLiteAnalyzedData;
import main.java.com.djrapitops.plan.data.PlanLitePlayerData;
import main.java.com.djrapitops.plan.ui.Html;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class Analysis {

    private Plan plugin;
    private InspectCacheHandler inspectCache;
    private final List<UserData> rawData;
    private HashMap<Long, ServerData> rawServerData;
    private final List<UUID> added;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public Analysis(Plan plugin) {
        this.plugin = plugin;
        this.inspectCache = plugin.getInspectCache();
        rawData = new ArrayList<>();
        added = new ArrayList<>();
    }

    /**
     * Analyzes the data of all offlineplayers on the server.
     *
     * First retrieves all Offlineplayers and checks those that are in the
     * database. Then Runs a new Analysis Task Asyncronously. Saves AnalysisData
     * to the provided Cache. Saves all UserData to InspectCache for 8 minutes.
     *
     * @param analysisCache Cache that the data is saved to.
     */
    public void analyze(AnalysisCacheHandler analysisCache) {
        rawData.clear();
        added.clear();
        log(Phrase.ANALYSIS_START + "");
        OfflinePlayer[] offlinePlayers;
        try {
            offlinePlayers = plugin.getServer().getOfflinePlayers();
        } catch (IndexOutOfBoundsException e) {
            plugin.log(Phrase.ANALYSIS_FAIL_NO_PLAYERS + "");
            return;
        }
        final List<UUID> uuids = new ArrayList<>();
        log(Phrase.ANALYSIS_FETCH_PLAYERS + "");
        for (OfflinePlayer p : offlinePlayers) {
            UUID uuid = p.getUniqueId();
            if (plugin.getDB().wasSeenBefore(uuid)) {
                uuids.add(uuid);
            }
        }
        if (uuids.isEmpty()) {
            plugin.log(Phrase.ANALYSIS_FAIL_NO_DATA + "");
            return;
        }
        final boolean useAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        final int port = Settings.WEBSERVER_PORT.getNumber();
        final String alternativeIP = Settings.ALTERNATIVE_IP.toString().replaceAll("%port%", "" + port);
        (new BukkitRunnable() {
            @Override
            public void run() {
                uuids.stream().forEach((uuid) -> {
                    inspectCache.cache(uuid, 8);
                });
                log(Phrase.ANALYSIS_FETCH_DATA + "");
                while (rawData.size() != uuids.size()) {
                    uuids.stream()
                            .filter((uuid) -> (!added.contains(uuid)))
                            .forEach((uuid) -> {
                                UserData userData = inspectCache.getFromCache(uuid);
                                if (userData != null) {
                                    rawData.add(userData);
                                    added.add(uuid);
                                }
                            });
                }
                rawServerData = plugin.getDB().getServerDataHashMap();
                log(Phrase.ANALYSIS_BEGIN_ANALYSIS + "");
                AnalysisData data = new AnalysisData();

                createPlayerActivityGraphs(data);

                // Create empty Dataset
                long gmZero = 0;
                long gmOne = 0;
                long gmTwo = 0;
                long gmThree = 0;

                long totalLoginTimes = 0;
                long totalPlaytime = 0;

                int totalBanned = 0;
                int active = 0;
                int joinleaver = 0;
                int inactive = 0;

                int ops = 0;
                List<Integer> ages = new ArrayList<>();

                boolean planLiteEnabled;
                PlanLiteHook planLiteHook = plugin.getPlanLiteHook();
                if (planLiteHook != null) {
                    planLiteEnabled = planLiteHook.isEnabled();
                } else {
                    planLiteEnabled = false;
                }

                PlanLiteAnalyzedData plData = new PlanLiteAnalyzedData();
                HashMap<String, Integer> townMap = new HashMap<>();
                HashMap<String, Integer> factionMap = new HashMap<>();
                int totalVotes = 0;
                int totalMoney = 0;

                HashMap<String, Long> latestLogins = new HashMap<>();
                HashMap<String, Long> playtimes = new HashMap<>();
                // Fill Dataset with userdata.
                for (UserData uData : rawData) {
                    if (planLiteEnabled) {
                        PlanLitePlayerData litePlayerData = uData.getPlanLiteData();
                        String town = litePlayerData.getTown();
                        if (!townMap.containsKey(town)) {
                            townMap.put(town, 0);
                        }
                        townMap.replace(town, townMap.get(town) + 1);
                        String faction = litePlayerData.getFaction();
                        if (!factionMap.containsKey(faction)) {
                            factionMap.put(faction, 0);
                        }
                        factionMap.replace(faction, factionMap.get(faction) + 1);
                        totalVotes += litePlayerData.getVotes();
                        totalMoney += litePlayerData.getMoney();
                    }
                    HashMap<GameMode, Long> gmTimes = uData.getGmTimes();
                    gmZero += gmTimes.get(GameMode.SURVIVAL);
                    gmOne += gmTimes.get(GameMode.CREATIVE);
                    gmTwo += gmTimes.get(GameMode.ADVENTURE);
                    try {
                        Long gm = gmTimes.get(GameMode.SPECTATOR);
                        if (gm != null) {
                            gmThree += gm;
                        }
                    } catch (NoSuchFieldError e) {
                    }
                    long playTime = uData.getPlayTime();
                    totalPlaytime += playTime;
                    String playerName = uData.getName();
                    String url = "http://" + (useAlternativeIP ? alternativeIP : plugin.getServer().getIp() + ":" + port)
                            + "/player/" + playerName;
                    String html = Html.BUTTON.parse(url, playerName);
                    playtimes.put(html, playTime);
                    latestLogins.put(html, uData.getLastPlayed());
                    totalLoginTimes += uData.getLoginTimes();
                    int age = uData.getDemData().getAge();
                    if (age != -1) {
                        ages.add(age);
                    }
                    if (uData.isOp()) {
                        ops++;
                    }

                    if (uData.isBanned()) {
                        totalBanned++;
                    } else if (uData.getLoginTimes() == 1) {
                        joinleaver++;
                    } else if (AnalysisUtils.isActive(uData.getLastPlayed(), playTime, uData.getLoginTimes())) {
                        active++;
                    } else {
                        inactive++;
                    }
                }

                // Save Dataset to AnalysisData
                data.setTop20ActivePlayers(AnalysisUtils.createActivePlayersTable(playtimes, 20));
                data.setRecentPlayers(AnalysisUtils.createListStringOutOfHashMapLong(latestLogins, 20));
                if (planLiteEnabled) {
                    plData.setFactionMap(factionMap);
                    plData.setTownMap(townMap);
                    plData.setTotalVotes(totalVotes);
                    plData.setTotalMoney(totalMoney);
                    data.setPlanLiteEnabled(true);
                    data.setPlanLiteData(plData);
                } else {
                    data.setPlanLiteEnabled(false);
                }

                data.setTotalLoginTimes(totalLoginTimes);

                String activityPieChartHtml = AnalysisUtils.createActivityPieChart(totalBanned, active, inactive, joinleaver);
                data.setActivityChartImgHtml(activityPieChartHtml);
                data.setActive(active);
                data.setInactive(inactive);
                data.setBanned(totalBanned);
                data.setJoinleaver(joinleaver);

                data.setTotal(offlinePlayers.length);
                data.setOps(ops);

                data.setTotalPlayTime(totalPlaytime);
                data.setAveragePlayTime(totalPlaytime / rawData.size());
                int totalAge = 0;
                for (int age : ages) {
                    totalAge += age;
                }
                double averageAge;
                if (!ages.isEmpty()) {
                    averageAge = totalAge * 1.0 / ages.size();
                } else {
                    averageAge = -1;
                }
                data.setAverageAge(averageAge);

                long gmTotal = gmZero + gmOne + gmTwo + gmThree;
                HashMap<GameMode, Long> totalGmTimes = new HashMap<>();
                totalGmTimes.put(GameMode.SURVIVAL, gmZero);
                totalGmTimes.put(GameMode.CREATIVE, gmOne);
                totalGmTimes.put(GameMode.ADVENTURE, gmTwo);
                try {
                    totalGmTimes.put(GameMode.SPECTATOR, gmThree);
                } catch (NoSuchFieldError e) {
                }
                String serverGMChartHtml = AnalysisUtils.createGMPieChart(totalGmTimes, gmTotal);
                data.setGmTimesChartImgHtml(serverGMChartHtml);
                data.setGm0Perc((gmZero * 1.0 / gmTotal));
                data.setGm1Perc((gmOne * 1.0 / gmTotal));
                data.setGm2Perc((gmTwo * 1.0 / gmTotal));
                data.setGm3Perc((gmThree * 1.0 / gmTotal));

                if (rawServerData.keySet().size() > 0) {
                    ServerData sData = null;
                    for (long sDataKey : rawServerData.keySet()) {
                        sData = rawServerData.get(sDataKey);
                        break;
                    }
                    if (sData != null) {
                        data.setTop50CommandsListHtml(AnalysisUtils.createTableOutOfHashMap(sData.getCommandUsage()));
                    }
                } else {
                    data.setTop50CommandsListHtml(Html.ERROR_TABLE.parse());
                }

                data.setRefreshDate(new Date().getTime());
                analysisCache.cache(data);
                plugin.log(Phrase.ANALYSIS_COMPLETE + "");
                this.cancel();
            }

            private void createPlayerActivityGraphs(AnalysisData data) {
                long scaleMonth = (long) 2592000 * (long) 1000;
                String playerActivityHtmlMonth = AnalysisUtils.createPlayerActivityGraph(rawServerData, scaleMonth);
                data.setPlayersChartImgHtmlMonth(playerActivityHtmlMonth);
                long scaleWeek = 604800 * 1000;
                String playerActivityHtmlWeek = AnalysisUtils.createPlayerActivityGraph(rawServerData, scaleWeek);
                data.setPlayersChartImgHtmlWeek(playerActivityHtmlWeek);
                long scaleDay = 86400 * 1000;
                String playerActivityHtmlDay = AnalysisUtils.createPlayerActivityGraph(rawServerData, scaleDay);
                data.setPlayersChartImgHtmlDay(playerActivityHtmlDay);
            }
        }).runTaskAsynchronously(plugin);
    }

    private void log(String msg) {
        if (Settings.ANALYSIS_LOG_TO_CONSOLE.isTrue()) {
            plugin.log(msg);
        }
    }
}