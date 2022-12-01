package cn.imokkkk.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * @author ImOkkkk
 * @date 2022/11/22 14:08
 * @since 1.0
 */
public class GuardedObject<T> {
  // 保存所有 GuardedObject
  static final Map<Object, GuardedObject> gos = new ConcurrentHashMap<>();
  final Lock lock = new ReentrantLock();
  final Condition done = lock.newCondition();
  final int timeout = 1;
  // 受保护的对象
  T obj;

  // 静态方法创建 GuardedObject
  static <K> GuardedObject create(K key) {
    GuardedObject go = new GuardedObject();
    gos.put(key, go);
    return go;
  }

  static <K, T> void fireEvent(K key, T obj) {
    GuardedObject go = gos.remove(key);
    if (go != null) {
      go.onChanged(obj);
    }
  }

  // 事件通知方法
  void onChanged(T obj) {
    lock.lock();
    try {
      this.obj = obj;
      done.signalAll();
    } finally {
      lock.unlock();
    }
  }

  // 获取受保护的对象
  T get(Predicate<T> p) {
    lock.lock();
    try {
      // MESA 管程推荐写法
      while (!p.test(obj)) {
        done.await(timeout, TimeUnit.SECONDS);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
    // 返回非空的受保护对象
    return obj;
  }

  // 事件通知方法
  void onChange(T obj) {
    lock.lock();
    try {
      this.obj = obj;
      done.signalAll();
    } finally {
      lock.unlock();
    }
  }
}
