// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.model.catalog.client;

import com.elfmcys.ysm.model.catalog.client.entry.ClientCatalogEntry;
import com.elfmcys.ysm.model.catalog.client.entry.ClientFailedCatalogEntry;
import com.elfmcys.ysm.model.catalog.snapshot.CatalogRecord;
import com.elfmcys.ysm.model.catalog.source.CatalogRootKind;

import com.elfmcys.ysm.model.catalog.snapshot.CatalogSnapshot;
import com.elfmcys.ysm.model.catalog.content.ModelContent;
import com.elfmcys.ysm.model.domain.ModelId;
import com.elfmcys.ysm.model.domain.PublishedModelId;
import com.elfmcys.ysm.model.domain.ModelPackDescriptor;
import com.elfmcys.ysm.model.resource.client.remote.RemoteModelContent;
import com.elfmcys.ysm.model.session.client.state.RemotePublicationSnapshot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Client presentation adapter; content lookup remains in the wrapped Catalog snapshot. */
public final class ClientCatalogSnapshot {
    private final CatalogSnapshot catalog;
    private final List<ClientFailedCatalogEntry> failed;
    private final RemotePublicationSnapshot publication;

    public ClientCatalogSnapshot(CatalogSnapshot catalog) {
        this(catalog, List.of());
    }

    public ClientCatalogSnapshot(CatalogSnapshot catalog, List<ClientFailedCatalogEntry> failed) {
        this(catalog, failed, null);
    }

    public ClientCatalogSnapshot(CatalogSnapshot catalog, List<ClientFailedCatalogEntry> failed,
        RemotePublicationSnapshot publication) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.failed = List.copyOf(failed);
        this.publication = publication;
    }

    public boolean permitsRuntimeContent(ModelId modelId) {
        var record = catalog.byModelId().get(modelId);
        if (record == null)
            return false;
        if (publication == null)
            return true;
        var entry = publication.entries().get(modelId);
        if (entry != null)
            return entry.runtimeContentAllowed();
        return false;
    }

    /**
     * Whether new use of a model is pending rather than denied: the session publication lists it with runtime content
     * allowed, but its activation has not finished (neither ready nor failed). The server sends the selection
     * directive right after the publication fragments, so a reselect can arrive while activation is still running.
     */
    public boolean awaitsRuntimeContent(ModelId modelId) {
        if (publication == null || catalog.byModelId().get(modelId) != null)
            return false;
        var entry = publication.entries().get(modelId);
        if (entry == null || !entry.runtimeContentAllowed())
            return false;
        return failed.stream().noneMatch(failure -> failure.modelId().equals(modelId));
    }

    public boolean awaitsRuntimeContent(PublishedModelId modelId) {
        return modelId instanceof PublishedModelId.Model model && awaitsRuntimeContent(model.hash());
    }

    public boolean permitsRuntimeContent(PublishedModelId modelId) {
        return modelId instanceof PublishedModelId.Model model ? permitsRuntimeContent(model.hash())
            : catalog.intrinsicDefault() != null;
    }

    public static ClientCatalogSnapshot empty() {
        return new ClientCatalogSnapshot(CatalogSnapshot.empty());
    }

    public Map<ModelId, ClientCatalogEntry> models() {
        var result = new LinkedHashMap<ModelId, ClientCatalogEntry>();
        catalog.byModelId().forEach((modelId, record) -> {
            var content = record.binding().content();
            result.put(modelId, new ClientCatalogEntry(record.entry(), content, origin(record, content)));
        });
        return Map.copyOf(result);
    }

    public List<ModelPackDescriptor> packs() {
        return catalog.packs();
    }

    public List<ClientFailedCatalogEntry> failed() {
        return failed;
    }

    public Optional<ClientCatalogEntry> find(ModelId modelId) {
        var record = catalog.byModelId().get(modelId);
        return record == null ? Optional.empty() : Optional.of(new ClientCatalogEntry(record.entry(),
            record.binding().content(), origin(record, record.binding().content())));
    }

    public Optional<ClientCatalogEntry> find(PublishedModelId modelId) {
        if (modelId instanceof PublishedModelId.Model model)
            return find(model.hash());
        var record = catalog.intrinsicDefault();
        return record == null ? Optional.empty() : Optional
            .of(new ClientCatalogEntry(record.entry(), record.binding().content(), ClientCatalogEntry.Origin.BUILTIN));
    }

    public CatalogSnapshot catalog() {
        return catalog;
    }

    private static ClientCatalogEntry.Origin origin(CatalogRecord record, ModelContent content) {
        var binding = content.storageBinding();
        if (binding != null) {
            return switch (binding.kind()) {
                case REMOTE -> ClientCatalogEntry.Origin.SERVER;
                case BUILTIN -> ClientCatalogEntry.Origin.BUILTIN;
                case LOCAL -> localOrigin(binding.localSource().orElseThrow().key().root().rootKind());
            };
        }
        if (content instanceof RemoteModelContent)
            return ClientCatalogEntry.Origin.SERVER;
        if (content instanceof com.elfmcys.ysm.model.storage.ManagedContainer local)
            return localOrigin(local.location().rootKind());
        return localOrigin(record.location().rootKind());
    }

    private static ClientCatalogEntry.Origin localOrigin(CatalogRootKind kind) {
        return switch (kind) {
            case BUILTIN -> ClientCatalogEntry.Origin.BUILTIN;
            case MODELS -> ClientCatalogEntry.Origin.MODELS;
        };
    }
}
