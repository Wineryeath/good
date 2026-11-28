package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.map.MartialSchoolFacility;
import com.jianghu.map.MartialSchoolInterior;
import com.jianghu.map.MartialSchoolSite;
import com.jianghu.time.WorldClock;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MartialSchoolInteriorTest {
    private GameWorld worldAt(String schoolId) {
        GameWorld world = new GameWorld(new Player("江湖客"), new WorldClock(1,360), List.of());
        var site = MartialSchoolSite.find(schoolId).orElseThrow();
        world.overworld().restorePosition(site.region(),site.x(),site.y());
        return world;
    }
    @Test void eachSchoolHasAnIndividualWalkableInterior() {
        for (var site : MartialSchoolSite.all()) {
            var interior = MartialSchoolInterior.create(site.schoolId());
            assertEquals(site.schoolId()+"-interior",interior.map().id());
            assertTrue(interior.map().isWalkable(interior.entranceX(),interior.entranceY()));
            assertEquals(4,interior.map().points().size());
            assertTrue(interior.map().points().stream().allMatch(p -> interior.map().isWalkable(p.x(),p.y())));
        }
    }
    @Test void playerCanEnterWalkToTrainingGroundUseItAndLeave() {
        GameWorld world=worldAt("shaolin-jingang");
        world.player().changeReputation(20);
        world.joinNearbyMartialSchool();
        assertTrue(world.enterNearbyMartialSchool().contains("进入少林金刚院"));
        assertTrue(world.insideMartialSchool());
        assertThrows(IllegalStateException.class, () -> world.useInsideMartialSchoolFacility(MartialSchoolFacility.Type.TRAINING_GROUND));
        for(int i=0;i<4;i++) assertTrue(world.moveInsideMartialSchool(-1,0));
        for(int i=0;i<5;i++) assertTrue(world.moveInsideMartialSchool(0,1));
        assertTrue(world.useInsideMartialSchoolFacility(MartialSchoolFacility.Type.TRAINING_GROUND).contains("修习"));
        assertFalse(world.insideMartialSchool());
    }
    @Test void visitorMayEnterButCannotUseSectFacilitiesAndLoadReturnsOutside() {
        GameWorld world=worldAt("wudang-zixiao");
        world.enterNearbyMartialSchool();
        assertThrows(IllegalStateException.class, () -> world.useInsideMartialSchoolFacility(MartialSchoolFacility.Type.MEDITATION_HALL));
        var snapshot=world.snapshot();
        world.restoreSnapshot(snapshot);
        assertFalse(world.insideMartialSchool());
        assertEquals("wudang-zixiao",world.nearbyMartialSchoolSite().orElseThrow().schoolId());
    }
}