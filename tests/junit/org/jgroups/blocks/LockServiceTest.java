package org.jgroups.blocks;

import org.jgroups.Global;
import org.jgroups.JChannel;
import org.jgroups.blocks.locking.AbstractLockService;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.blocks.locking.PeerLockService;
import org.jgroups.tests.ChannelTestBase;
import org.jgroups.util.Util;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.locks.Lock;

/** Tests {@link org.jgroups.blocks.locking.LockService}
 * @author Bela Ban
 */
@Test(groups=Global.STACK_DEPENDENT,sequential=true)
public class LockServiceTest extends ChannelTestBase {
    protected JChannel c1, c2, c3, c4;
    protected static final String LOCK="sample-lock";


    @BeforeTest
    protected void init() throws Exception {
        c1=createChannel(true, 4, "A");
        c1.connect("LockServiceTest");

        c2=createChannel(c1, "B");
        c2.connect("LockServiceTest");

        c3=createChannel(c1, "C");
        c3.connect("LockServiceTest");

        c4=createChannel(c1, "D");
        c4.connect("LockServiceTest");
    }

    @AfterTest
    protected void cleanup() {
        Util.close(c4,c3,c2,c1);
    }

    @DataProvider(name="createLockService")
    AbstractLockService[][] createLockService() {
        return new AbstractLockService[][] {
          {new PeerLockService(), new PeerLockService(), new PeerLockService(), new PeerLockService()}
        };
    }

    @Test(dataProvider="createLockService")
    public void testSimpleLock(AbstractLockService s1, AbstractLockService s2, AbstractLockService s3, AbstractLockService s4) {
        s1.setChannel(c1);
        s2.setChannel(c2);
        s3.setChannel(c3);
        s4.setChannel(c4);

        
        Lock lock=s1.getLock(LOCK);

        System.out.print("acquiring lock " + LOCK + ": ");
        lock.lock();
        try {
            System.out.println("OK");
            Util.sleep(1000);
            System.out.print("releasing lock " + LOCK + ": ");
        }
        finally {
            lock.unlock();
            System.out.println("OK");
        }
    }

    @Test(dataProvider="createLockService")
    public void testBlockingLock(AbstractLockService s1, AbstractLockService s2, AbstractLockService s3, AbstractLockService s4) throws InterruptedException {
        s1.setChannel(c1);
        s2.setChannel(c2);
        s3.setChannel(c3);
        s4.setChannel(c4);

        // todo: revisit test

//        final LockService lock_service=s1;
//        Thread other=new Thread() {
//            public void run() {
//                Lock l2=lock_service.getLock(LOCK);
//                lock(l2, LOCK);
//                Util.sleep(2000);
//                unlock(l2, LOCK);
//            }
//        };
//        other.start();
//
//        Util.sleep(500);
//
//        System.out.println("Locks on S1:\n" + s1.printLocks());
//
//        final Lock l1=s1.getLock(LOCK);
//        lock(l1, LOCK);
//        try {
//            ;
//        }
//        finally {
//            unlock(l1, LOCK);
//        }
//
//        other.join();
    }


    protected static void lock(Lock lock, String name) {
        System.out.println("[" + Thread.currentThread().getId() + "] locking " + name);
        lock.lock();
        System.out.println("[" + Thread.currentThread().getId() + "] locked " + name);
    }

    protected static void unlock(Lock lock, String name) {
        System.out.println("[" + Thread.currentThread().getId() + "] releasing " + name);
        lock.unlock();
        System.out.println("[" + Thread.currentThread().getId() + "] released " + name);
    }
}