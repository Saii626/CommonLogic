package app.saikat.ThreadManagement.interfaces;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import javax.inject.Provider;

import app.saikat.DIManagement.Impl.BeanManagers.InjectBeanManager;
import app.saikat.DIManagement.Impl.BeanManagers.PostConstructBeanManager;
import app.saikat.DIManagement.Impl.DIBeans.ConstantProviderBean;
import app.saikat.DIManagement.Interfaces.DIBean;

/**
 * Helper class to perform generic actions for scheduling
 * @param <PARENT> type of parent
 * @param <TYPE> type of the bean
 */
public abstract class TaskProvider<PARENT, TYPE> implements Provider<TYPE> {

    protected final DIBean<TYPE> weakInstanceCopy;
    protected final WeakReference<PARENT> weakParent;
    protected final ConstantProviderBean<PARENT> strongEmptyProvider;

    protected final InjectBeanManager injectBeanManager;
    protected final PostConstructBeanManager postConstructBeanManager;

    protected Consumer<TaskProvider<PARENT, TYPE>> onParentDied;

    @SuppressWarnings("unchecked")
    public TaskProvider(DIBean<TYPE> instanceTaskCopy, ConstantProviderBean<PARENT> strongProvider,
            InjectBeanManager injectBeanManager, PostConstructBeanManager postConstructBeanManager) {
        this.weakInstanceCopy = instanceTaskCopy;
        this.strongEmptyProvider = strongProvider;

        this.weakParent = (WeakReference<PARENT>) this.weakInstanceCopy.getDependencies()
                .get(0)
                .getProvider()
                .get();

        this.injectBeanManager = injectBeanManager;
        this.postConstructBeanManager = postConstructBeanManager;
    }

    public void setOnParentDied(Consumer<TaskProvider<PARENT, TYPE>> onParentDied) {
        this.onParentDied = onParentDied;
    }

    @Override
    public TYPE get() {
        ConstantProviderBean<PARENT> parent = extractStrongReference();

        if (parent != null) {
            DIBean<TYPE> invocationTaskCopy = this.weakInstanceCopy.copy();
            invocationTaskCopy.getDependencies()
                    .set(0, parent);

            return executeBean(invocationTaskCopy);

        } else {
            onParentDied.accept(this);
            return null;
        }
    }

    protected ConstantProviderBean<PARENT> extractStrongReference() {
        PARENT parent = weakParent.get();

        if (parent != null) {
            ConstantProviderBean<PARENT> providerBean = strongEmptyProvider.copy();
            providerBean.setProvider(() -> (PARENT) parent);
            return providerBean;
        } else {
            return null;
        }
    }

    protected abstract TYPE executeBean(DIBean<TYPE> bean);

    WeakReference<PARENT> getWeakParent() {
        return weakParent;
    }
}