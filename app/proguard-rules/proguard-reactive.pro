#build.gradle
#
#    compile 'io.reactivex:rxandroid:1.0.1'
#    compile 'io.reactivex:rxjava:1.0.14'
#    compile 'io.reactivex:rxjava-math:1.0.0'
#    compile 'com.jakewharton.rxbinding:rxbinding:0.2.0'

# rxjava
-keep class io.reactivex.schedulers.Schedulers {
    public static <methods>;
}
-keep class io.reactivex.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class io.reactivex.schedulers.TestScheduler {
    public <methods>;
}
-keep class io.reactivex.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class io.reactivex.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class io.reactivex.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}