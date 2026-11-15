package com.adamkali.dwm.network;

import com.adamkali.dwm.MinecraftTestBootstrap;
import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArsPayloadsTest {
    private static final Identifier LIBRARY = Identifier.fromNamespaceAndPath("dwm", "library");
    private static final Identifier STRAIGHT = Identifier.fromNamespaceAndPath("dwm", "corridor_straight");

    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    @Test
    void openArsScreen_roundTripsLayoutAndArtron() {
        OpenArsScreen payload = new OpenArsScreen(UUID.randomUUID(), List.of(
                new OpenArsScreen.PlacedPiece(STRAIGHT, 2, 1, 1),
                new OpenArsScreen.PlacedPiece(LIBRARY, 4, 0, 3)
        ), 375, false);

        OpenArsScreen decoded = roundTrip(OpenArsScreen.CODEC, payload);

        assertEquals(payload, decoded);
        assertEquals(OpenArsScreen.ID, decoded.type());
    }

    @Test
    void openArsScreen_roundTripsEmptyLayout() {
        OpenArsScreen payload = new OpenArsScreen(UUID.randomUUID(), List.of(), 0, true);

        OpenArsScreen decoded = roundTrip(OpenArsScreen.CODEC, payload);
        assertEquals(payload, decoded);
        assertTrue(decoded.refresh());
    }

    @Test
    void openArsScreen_ofSkipsUnparseableSavedIdsAndCarriesArtron() {
        TardisDataModel model = new TardisDataModel();
        model.artron = 120;
        model.getArsPieces().add(new ArsPlacedPiece(STRAIGHT.toString(), 2, 1, 1));
        model.getArsPieces().add(new ArsPlacedPiece("Not An Id!", 3, 1, 0));
        model.getArsPieces().add(new ArsPlacedPiece(null, 3, 2, 0));
        model.getArsPieces().add(null);
        UUID id = UUID.randomUUID();

        OpenArsScreen payload = OpenArsScreen.of(id, model, true);

        assertEquals(id, payload.tardisId());
        assertEquals(List.of(new OpenArsScreen.PlacedPiece(STRAIGHT, 2, 1, 1)), payload.layout());
        assertEquals(120, payload.artron());
        assertTrue(payload.refresh());
        assertEquals(List.of(new ArsPlacedPiece(STRAIGHT.toString(), 2, 1, 1)), payload.placedPieces());
    }

    @Test
    void openArsScreen_ofNullModelIsEmpty() {
        OpenArsScreen payload = OpenArsScreen.of(UUID.randomUUID(), null, false);

        assertTrue(payload.layout().isEmpty());
        assertEquals(0, payload.artron());
        assertFalse(payload.refresh());
    }

    @Test
    void growPayload_roundTripsIncludingExtremeInts() {
        UUID id = UUID.randomUUID();
        for (ArsGrowC2SPayload payload : List.of(
                new ArsGrowC2SPayload(id, LIBRARY, 4, 0, 3),
                new ArsGrowC2SPayload(id, STRAIGHT, Integer.MIN_VALUE, Integer.MAX_VALUE, -1))) {
            ArsGrowC2SPayload decoded = roundTrip(ArsGrowC2SPayload.CODEC, payload);
            assertEquals(payload, decoded);
            assertEquals(ArsGrowC2SPayload.ID, decoded.type());
        }
    }

    @Test
    void jettisonPayload_roundTripsIncludingExtremeInts() {
        UUID id = UUID.randomUUID();
        for (ArsJettisonC2SPayload payload : List.of(
                new ArsJettisonC2SPayload(id, 3, 1),
                new ArsJettisonC2SPayload(id, Integer.MAX_VALUE, Integer.MIN_VALUE))) {
            ArsJettisonC2SPayload decoded = roundTrip(ArsJettisonC2SPayload.CODEC, payload);
            assertEquals(payload, decoded);
            assertEquals(ArsJettisonC2SPayload.ID, decoded.type());
        }
    }

    private static <T> T roundTrip(StreamCodec<RegistryFriendlyByteBuf, T> codec, T value) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        codec.encode(buf, value);
        T decoded = codec.decode(buf);
        assertEquals(0, buf.readableBytes(), "codec left unread bytes");
        return decoded;
    }
}
