package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.combat.MartialSchool;
import com.jianghu.map.MartialSchoolSite;
import com.jianghu.map.OverworldRegion;
import com.jianghu.save.GameWorldSnapshotCodec;
import com.jianghu.time.WorldClock;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MartialSchoolProgressionTest {
    private GameWorld world() { return new GameWorld(new Player("求道者"), new WorldClock(1, 360), List.of()); }

    @Test void allSchoolSitesAreWalkableAndResolveToTheirSchool() {
        assertEquals(4, MartialSchoolSite.all().size());
        for (MartialSchoolSite site : MartialSchoolSite.all()) {
            assertTrue(com.jianghu.map.OverworldRegionCatalog.find(site.region()).isWalkable(site.x(), site.y()));
            assertEquals(site.schoolId(), site.school().id());
            assertTrue(MartialSchool.find(site.schoolId()).isPresent());
        }
    }

    @Test void playerCanJoinTrainAndEarnShaolinSignatureTechnique() {
        GameWorld world = world();
        MartialSchoolSite site = MartialSchoolSite.find("shaolin-jingang").orElseThrow();
        world.overworld().restorePosition(site.region(), site.x(), site.y());
        world.player().changeReputation(15);
        long moneyBefore = world.player().moneyInWen();
        assertTrue(world.inspectNearbyMartialSchool().contains("少林·金刚院"));
        assertTrue(world.joinNearbyMartialSchool().contains("正式入门"));
        assertTrue(world.schoolMemberships().contains("shaolin-jingang"));
        assertEquals(moneyBefore - 500, world.player().moneyInWen());
        for (int i = 0; i < 3; i++) {
            assertTrue(world.trainAtNearbyMartialSchool().contains("师门信任"));
            if (i < 2) world.advanceMinutes(1440);
        }
        assertEquals(3, world.martialSchoolFavor("shaolin-jingang"));
        assertEquals(30, world.martialSchoolContribution("shaolin-jingang"));
        assertEquals(com.jianghu.combat.MartialSchoolRank.OUTER_DISCIPLE, world.martialSchoolRank("shaolin-jingang"));
        assertTrue(world.player().knowsTechnique("sweeping_staff"));
        assertTrue(world.player().learnedStyles().contains(com.jianghu.combat.MartialArtStyle.STAFF));
    }

    @Test void schoolMembershipAndTrainingProgressSurviveWorldSaveRoundTrip() {
        GameWorld original = world();
        MartialSchoolSite site = MartialSchoolSite.find("wudang-zixiao").orElseThrow();
        original.overworld().restorePosition(site.region(), site.x(), site.y());
        original.player().changeReputation(8); original.joinNearbyMartialSchool();
        original.trainAtNearbyMartialSchool();
        var encoded = GameWorldSnapshotCodec.encode(original.snapshot());
        GameWorld restored = world();
        restored.restoreSnapshot(GameWorldSnapshotCodec.decode(encoded));
        assertEquals(original.schoolMemberships(), restored.schoolMemberships());
        assertEquals(original.martialSchoolFavor("wudang-zixiao"), restored.martialSchoolFavor("wudang-zixiao"));
        assertEquals(original.snapshot().schoolTrainingDays(), restored.snapshot().schoolTrainingDays());
        assertEquals(original.snapshot().schoolContribution(), restored.snapshot().schoolContribution());
        assertEquals(original.martialSchoolRank("wudang-zixiao"), restored.martialSchoolRank("wudang-zixiao"));
        assertTrue(restored.player().learnedStyles().contains(com.jianghu.combat.MartialArtStyle.SWORD));
    }
    @Test void donationsAdvanceRankAndRejectInvalidOrUnaffordableAmounts() {
        GameWorld world = world();
        MartialSchoolSite site = MartialSchoolSite.find("emei-lingxiu").orElseThrow();
        world.overworld().restorePosition(site.region(), site.x(), site.y());
        world.joinNearbyMartialSchool();
        assertThrows(IllegalArgumentException.class, () -> world.donateToNearbyMartialSchool(150));
        assertThrows(IllegalStateException.class, () -> world.donateToNearbyMartialSchool(100_000));
        assertTrue(world.donateToNearbyMartialSchool(300).contains("晋升"));
        assertEquals(30, world.martialSchoolContribution("emei-lingxiu"));
        assertEquals(com.jianghu.combat.MartialSchoolRank.OUTER_DISCIPLE, world.martialSchoolRank("emei-lingxiu"));
        assertThrows(IllegalStateException.class, () -> world.donateToNearbyMartialSchool(100));
    }
}