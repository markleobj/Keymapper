package io.github.sds100.keymapper.purchasing;

import io.github.sds100.keymapper.base.purchasing.PurchasingError;
import io.github.sds100.keymapper.base.purchasing.PurchasingManager;
import io.github.sds100.keymapper.base.purchasing.RevenueCatEntitlementId;
import io.github.sds100.keymapper.common.utils.KMResult;
import io.github.sds100.keymapper.common.utils.State;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u0006H\u0096@\u00a2\u0006\u0002\u0010\u0011J\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00100\u0006H\u0096@\u00a2\u0006\u0002\u0010\u0011J\u001c\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u00062\u0006\u0010\u0015\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0016J\u001c\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u00062\u0006\u0010\u0015\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0016J\u001c\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\u00062\u0006\u0010\u001a\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\u001bJ\u001c\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00190\u00062\u0006\u0010\u0015\u001a\u00020\u0010H\u0096@\u00a2\u0006\u0002\u0010\u0016J*\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001e0\u00062\u0006\u0010\u0015\u001a\u00020\u00102\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\b0 H\u0096@\u00a2\u0006\u0002\u0010!J\b\u0010\"\u001a\u00020\u001eH\u0016J\u001a\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006H\u0096@\u00a2\u0006\u0002\u0010\u0011J\u0010\u0010$\u001a\u00020\u001e2\u0006\u0010%\u001a\u00020\u0010H\u0016R,\u0010\u0003\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00060\u00050\u0004X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001a\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\b0\fX\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006&"}, d2 = {"Lio/github/sds100/keymapper/purchasing/PurchasingManagerImpl;", "Lio/github/sds100/keymapper/base/purchasing/PurchasingManager;", "()V", "entitlements", "Lkotlinx/coroutines/flow/Flow;", "Lio/github/sds100/keymapper/common/utils/State;", "Lio/github/sds100/keymapper/common/utils/KMResult;", "", "Lio/github/sds100/keymapper/base/purchasing/RevenueCatEntitlementId;", "getEntitlements", "()Lkotlinx/coroutines/flow/Flow;", "onCompleteProductPurchase", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "getOnCompleteProductPurchase", "()Lkotlinx/coroutines/flow/MutableSharedFlow;", "getCurrentOfferingId", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCustomerId", "getNonSubscriptionPurchaseCount", "", "packageId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPackagePrice", "hasEntitlement", "", "entitlement", "(Lio/github/sds100/keymapper/base/purchasing/RevenueCatEntitlementId;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isPackagePurchased", "launchPurchasingFlow", "", "verifyEntitlements", "", "(Ljava/lang/String;[Lio/github/sds100/keymapper/base/purchasing/RevenueCatEntitlementId;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refresh", "restorePurchases", "trackCustomPaywallImpression", "paywallIdentifier", "app_debug"})
public final class PurchasingManagerImpl implements io.github.sds100.keymapper.base.purchasing.PurchasingManager {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<io.github.sds100.keymapper.base.purchasing.RevenueCatEntitlementId> onCompleteProductPurchase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<io.github.sds100.keymapper.common.utils.State<io.github.sds100.keymapper.common.utils.KMResult<java.util.Set<io.github.sds100.keymapper.base.purchasing.RevenueCatEntitlementId>>>> entitlements = null;
    
    public PurchasingManagerImpl() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.MutableSharedFlow<io.github.sds100.keymapper.base.purchasing.RevenueCatEntitlementId> getOnCompleteProductPurchase() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<io.github.sds100.keymapper.common.utils.State<io.github.sds100.keymapper.common.utils.KMResult<java.util.Set<io.github.sds100.keymapper.base.purchasing.RevenueCatEntitlementId>>>> getEntitlements() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object launchPurchasingFlow(@org.jetbrains.annotations.NotNull()
    java.lang.String packageId, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.purchasing.RevenueCatEntitlementId[] verifyEntitlements, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super io.github.sds100.keymapper.common.utils.KMResult<kotlin.Unit>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object isPackagePurchased(@org.jetbrains.annotations.NotNull()
    java.lang.String packageId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super io.github.sds100.keymapper.common.utils.KMResult<java.lang.Boolean>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getNonSubscriptionPurchaseCount(@org.jetbrains.annotations.NotNull()
    java.lang.String packageId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super io.github.sds100.keymapper.common.utils.KMResult<java.lang.Integer>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getPackagePrice(@org.jetbrains.annotations.NotNull()
    java.lang.String packageId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super io.github.sds100.keymapper.common.utils.KMResult<java.lang.String>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object hasEntitlement(@org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.purchasing.RevenueCatEntitlementId entitlement, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super io.github.sds100.keymapper.common.utils.KMResult<java.lang.Boolean>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getCurrentOfferingId(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super io.github.sds100.keymapper.common.utils.KMResult<java.lang.String>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object restorePurchases(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super io.github.sds100.keymapper.common.utils.KMResult<? extends java.util.Set<? extends io.github.sds100.keymapper.base.purchasing.RevenueCatEntitlementId>>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getCustomerId(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super io.github.sds100.keymapper.common.utils.KMResult<java.lang.String>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    public void refresh() {
    }
    
    @java.lang.Override()
    public void trackCustomPaywallImpression(@org.jetbrains.annotations.NotNull()
    java.lang.String paywallIdentifier) {
    }
}