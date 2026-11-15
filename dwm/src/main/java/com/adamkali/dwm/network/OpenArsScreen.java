package com.adamkali.dwm.network;

import com.adamkali.dwm.tardis.data.model.ArsPlacedPiece;
import com.adamkali.dwm.tardis.data.model.TardisDataModel;
import com.adamkali.dwm.tardis.logic.ArtronLogic;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Opens (or refreshes) the ARS map screen: the TARDIS's current layout and artron. Piece definitions are not
 * sent; the client reads them from the synced {@code ArsRegistries.PIECE} registry.
 */
public record OpenArsScreen(UUID tardisId, List<PlacedPiece> layout, int artron) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenArsScreen> ID =
            new CustomPacketPayload.Type<>(DWMPacketIds.OPEN_ARS_SCREEN_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlacedPiece> PIECE_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, PlacedPiece::pieceId,
            ByteBufCodecs.VAR_INT, PlacedPiece::cellX,
            ByteBufCodecs.VAR_INT, PlacedPiece::cellZ,
            ByteBufCodecs.VAR_INT, PlacedPiece::rotation,
            PlacedPiece::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenArsScreen> CODEC = StreamCodec.composite(
            DWMPacketCodecs.UUID_PACKET_CODEC, OpenArsScreen::tardisId,
            PIECE_CODEC.apply(ByteBufCodecs.list()), OpenArsScreen::layout,
            ByteBufCodecs.VAR_INT, OpenArsScreen::artron,
            OpenArsScreen::new
    );

    public OpenArsScreen {
        layout = List.copyOf(layout);
    }

    /**
     * Snapshot of {@code model}'s layout and artron. Saved pieces whose id does not parse are skipped (the
     * layout rules ignore them too); a null model gives an empty layout and no artron.
     */
    public static OpenArsScreen of(UUID tardisId, @Nullable TardisDataModel model) {
        if (model == null) {
            return new OpenArsScreen(tardisId, List.of(), 0);
        }
        List<PlacedPiece> pieces = new ArrayList<>(model.getArsPieces().size());
        for (ArsPlacedPiece piece : model.getArsPieces()) {
            if (piece == null || piece.pieceId == null) {
                continue;
            }
            Identifier id = Identifier.tryParse(piece.pieceId);
            if (id == null) {
                continue;
            }
            pieces.add(new PlacedPiece(id, piece.cellX, piece.cellZ, piece.rotation));
        }
        return new OpenArsScreen(tardisId, pieces, ArtronLogic.read(model));
    }

    /** The layout as {@link ArsPlacedPiece}s, for {@code ArsLayoutLogic}. */
    public List<ArsPlacedPiece> placedPieces() {
        List<ArsPlacedPiece> pieces = new ArrayList<>(layout.size());
        for (PlacedPiece piece : layout) {
            pieces.add(piece.toPlaced());
        }
        return pieces;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    /** One placed piece: definition id, anchor cell and rotation (clockwise quarter turns). */
    public record PlacedPiece(Identifier pieceId, int cellX, int cellZ, int rotation) {
        public ArsPlacedPiece toPlaced() {
            return new ArsPlacedPiece(pieceId.toString(), cellX, cellZ, rotation);
        }
    }
}
