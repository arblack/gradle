/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.component.external.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import jdk.internal.jline.internal.Nullable;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.capabilities.CapabilitiesMetadata;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.internal.component.model.ComponentArtifactMetadata;
import org.gradle.internal.component.model.DefaultVariantMetadata;
import org.gradle.internal.component.model.ExcludeMetadata;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.internal.component.model.ModuleConfigurationMetadata;
import org.gradle.internal.component.model.VariantResolveMetadata;

import java.util.List;
import java.util.Set;

/**
 * A configuration representing an additional variant of a published component added by a component metadata rule.
 * It can be backed by an existing configuration/variant (base) or can initially be empty (base = null).
 */
class LazyRuleAwareWithBaseConfigurationMetadata implements ModuleConfigurationMetadata {

    private final String name;
    private final ModuleConfigurationMetadata base;
    private final ModuleComponentIdentifier componentId;
    private final VariantMetadataRules variantMetadataRules;

    private List<? extends ModuleDependencyMetadata> computedDependencies;
    private ImmutableAttributes computedAttributes;
    private CapabilitiesMetadata computedCapabilities;
    private ImmutableList<? extends ComponentArtifactMetadata> computedArtifacts;
    private ImmutableAttributes componentLevelAttributes;

    LazyRuleAwareWithBaseConfigurationMetadata(String name, @Nullable ModuleConfigurationMetadata base, ModuleComponentIdentifier componentId, ImmutableAttributes componentLevelAttributes, VariantMetadataRules variantMetadataRules) {
        this.name = name;
        this.base = base;
        this.componentId = componentId;
        this.variantMetadataRules = variantMetadataRules;
        this.componentLevelAttributes = componentLevelAttributes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<? extends ModuleDependencyMetadata> getDependencies() {
        if (computedDependencies == null) {
            computedDependencies = variantMetadataRules.applyDependencyMetadataRules(this, base == null ? ImmutableList.of() : base.getDependencies());
        }
        return computedDependencies;
    }

    @Override
    public ImmutableAttributes getAttributes() {
        if (computedAttributes == null) {
            computedAttributes = variantMetadataRules.applyVariantAttributeRules(this, base == null ? componentLevelAttributes : base.getAttributes());
        }
        return computedAttributes;
    }

    @Override
    public ImmutableList<? extends ComponentArtifactMetadata> getArtifacts() {
        if (computedArtifacts == null) {
            computedArtifacts = variantMetadataRules.applyVariantFilesMetadataRulesToArtifacts(this, base == null ? ImmutableList.of() : base.getArtifacts(), componentId);
        }
        return computedArtifacts;
    }

    @Override
    public CapabilitiesMetadata getCapabilities() {
        if (computedCapabilities == null) {
            computedCapabilities = variantMetadataRules.applyCapabilitiesRules(this, base == null ? ImmutableCapabilities.EMPTY : base.getCapabilities());
        }
        return computedCapabilities;
    }

    @Override
    public boolean requiresMavenArtifactDiscovery() {
        return false;
    }

    @Override
    public Set<? extends VariantResolveMetadata> getVariants() {
        return ImmutableSet.of(new DefaultVariantMetadata(asDescribable(), getAttributes(), getArtifacts(), getCapabilities()));
    }

    @Override
    public DisplayName asDescribable() {
        return Describables.of(componentId, "configuration", name);
    }

    @Override
    public ComponentArtifactMetadata artifact(IvyArtifactName artifact) {
        return new DefaultModuleComponentArtifactMetadata(componentId, artifact);
    }

    @Override
    public ImmutableSet<String> getHierarchy() {
        return ImmutableSet.of(name);
    }

    @Override
    public ImmutableList<ExcludeMetadata> getExcludes() {
        return ImmutableList.of();
    }

    @Override
    public boolean isTransitive() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean isCanBeConsumed() {
        return true;
    }

    @Override
    public boolean isCanBeResolved() {
        return false;
    }

    @Override
    public List<String> getConsumptionAlternatives() {
        return ImmutableList.of();
    }
}
