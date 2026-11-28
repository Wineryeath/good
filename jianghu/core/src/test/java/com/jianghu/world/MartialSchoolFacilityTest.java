package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.map.MartialSchoolFacility;
import com.jianghu.map.MartialSchoolSite;
import com.jianghu.time.WorldClock;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MartialSchoolFacilityTest {
    private GameWorld world() { return new GameWorld(new Player("行走江湖"), new WorldClock(1, 360), List.of()); }

    @Test void everySchoolHasFourDistinctFacilities() {
        assertEquals(16, MartialSchoolFacility.all().size());
        for (var site : MartialSchoolSite.all()) {
            var facilities = MartialSchoolFacility.forSchool(site.schoolId());
            assertEquals(4, facilities.size());
            assertEquals(4, facilities.stream().map(MartialSchoolFacility::type).distinct().count());
            assertTrue(facilities.stream().allMatch(f -> f.id().startsWith(site.schoolId() + "-")));
        }
    }

    @Test void facilitiesAreVisibleNearSchoolButRequireMembership() {
        GameWorld world = world();
        var site = MartialSchoolSite.find("shaolin-jingang").orElseThrow();
        world.overworld().restorePosition(site.region(), site.x(), site.y());
        assertEquals(4, world.nearbyMartialSchoolFacilities().size());
        assertThrows(IllegalStateException.class, () -> world.useNearbyMartialSchoolFacility("shaolin-jingang-meditation"));
        world.player().changeReputation(15);
        world.joinNearbyMartialSchool();
        assertTrue(world.useNearbyMartialSchoolFacility("shaolin-jingang-scripture").contains("研读本门典籍"));
        assertEquals(12, world.player().martialExperience());
    }

    @Test void meditationAndInfirmaryApplyRealPlayerEffects() {
        GameWorld world = world();
        var site = MartialSchoolSite.find("wudang-zixiao").orElseThrow();
        world.overworld().restorePosition(site.region(), site.x(), site.y());
        world.player().changeReputation(10);
        world.joinNearbyMartialSchool();
        world.player().spendInnerEnergy(50);
        world.player().spendStamina(40);
        world.useNearbyMartialSchoolFacility("wudang-zixiao-meditation");
        assertEquals(90, world.player().innerEnergy());
        assertEquals(68, world.player().stamina());
        world.player().changeHealth(-50);
        world.player().spendStamina(20);
        long before = world.player().moneyInWen();
        world.useNearbyMartialSchoolFacility("wudang-zixiao-infirmary");
        assertEquals(85, world.player().health());
        assertEquals(before - 120, world.player().moneyInWen());
        assertThrows(IllegalArgumentException.class, () -> world.useNearbyMartialSchoolFacility("shaolin-jingang-infirmary"));
    }
}