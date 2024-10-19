package org.example;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TripletDeque<E> implements Deque<E>, Containerable {
    private static final int DEFAULT_CONTAINER_SIZE = 5;
    private static final int DEFAULT_MAX_SIZE = 1000;

    private class Container {
        E[] elements;
        int size;

        Container() {
            elements = (E[]) new Object[DEFAULT_CONTAINER_SIZE]; // Исправление здесь
        }
    }

    private Container[] containers;
    private int firstContainerIndex;
    private int lastContainerIndex;
    private int firstIndexInFirstContainer;
    private int lastIndexInLastContainer;
    private int size;
    private final int maxSize;

    public TripletDeque() {
        this(DEFAULT_MAX_SIZE);
    }

    public TripletDeque(int maxSize) {
        this.maxSize = maxSize;
        containers = new Container[maxSize / DEFAULT_CONTAINER_SIZE + 1];
        containers[0] = new Container();
        firstContainerIndex = 0;
        lastContainerIndex = 0;
        firstIndexInFirstContainer = 0;
        lastIndexInLastContainer = 0;
    }

    private void ensureCapacity() {
        Container[] newContainers = new Container[containers.length * 2];
        System.arraycopy(containers, 0, newContainers, 0, containers.length);
        containers = newContainers;
    }

    @Override
    public Object[] getContainerByIndex(int cIndex) {
        if (cIndex < 0 || cIndex >= containers.length || containers[cIndex] == null) {
            return null;
        }
        return containers[cIndex].elements;
    }

    @Override
    public void addFirst(E e) {
        if (size >= maxSize) {
            throw new IllegalStateException("Deque is full");
        }
        if (firstIndexInFirstContainer == 0) {
            if (firstContainerIndex == 0) {
                ensureCapacity();
            }
            firstContainerIndex--;
            if (containers[firstContainerIndex] == null) {
                containers[firstContainerIndex] = new Container();
            }
            firstIndexInFirstContainer = DEFAULT_CONTAINER_SIZE;
        }
        firstIndexInFirstContainer--;
        containers[firstContainerIndex].elements[firstIndexInFirstContainer] = e;
        size++;
    }

    @Override
    public void addLast(E e) {
        if (size >= maxSize) {
            throw new IllegalStateException("Deque is full");
        }
        if (lastIndexInLastContainer == DEFAULT_CONTAINER_SIZE - 1) {
            if (lastContainerIndex == containers.length - 1) {
                ensureCapacity();
            }
            lastContainerIndex++;
            if (containers[lastContainerIndex] == null) {
                containers[lastContainerIndex] = new Container();
            }
            lastIndexInLastContainer = -1;
        }
        lastIndexInLastContainer++;
        containers[lastContainerIndex].elements[lastIndexInLastContainer] = e;
        size++;
    }

    @Override
    public boolean offerFirst(E e) {
        if (size >= maxSize) {
            return false;
        }
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        if (size >= maxSize) {
            return false;
        }
        addLast(e);
        return true;
    }

    @Override
    public E removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        E element = containers[firstContainerIndex].elements[firstIndexInFirstContainer];
        containers[firstContainerIndex].elements[firstIndexInFirstContainer] = null;
        firstIndexInFirstContainer++;
        if (firstIndexInFirstContainer == DEFAULT_CONTAINER_SIZE) {
            firstContainerIndex++;
            firstIndexInFirstContainer = 0;
        }
        size--;
        return element;
    }

    @Override
    public E removeLast() {
        if (size == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        E element = containers[lastContainerIndex].elements[lastIndexInLastContainer];
        containers[lastContainerIndex].elements[lastIndexInLastContainer] = null;
        lastIndexInLastContainer--;
        if (lastIndexInLastContainer == -1) {
            lastContainerIndex--;
            lastIndexInLastContainer = DEFAULT_CONTAINER_SIZE - 1;
        }
        size--;
        return element;
    }

    @Override
    public E pollFirst() {
        if (size == 0) {
            return null;
        }
        return removeFirst();
    }

    @Override
    public E pollLast() {
        if (size == 0) {
            return null;
        }
        return removeLast();
    }

    @Override
    public E getFirst() {
        if (size == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        return containers[firstContainerIndex].elements[firstIndexInFirstContainer];
    }

    @Override
    public E getLast() {
        if (size == 0) {
            throw new NoSuchElementException("Deque is empty");
        }
        return containers[lastContainerIndex].elements[lastIndexInLastContainer];
    }

    @Override
    public E peekFirst() {
        if (size == 0) {
            return null;
        }
        return containers[firstContainerIndex].elements[firstIndexInFirstContainer];
    }

    @Override
    public E peekLast() {
        if (size == 0) {
            return null;
        }
        return containers[lastContainerIndex].elements[lastIndexInLastContainer];
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        for (int i = firstContainerIndex; i <= lastContainerIndex; i++) {
            Container container = containers[i];
            for (int j = (i == firstContainerIndex ? firstIndexInFirstContainer : 0); j < DEFAULT_CONTAINER_SIZE; j++) {
                if (Objects.equals(o, container.elements[j])) {
                    System.arraycopy(container.elements, j + 1, container.elements, j, DEFAULT_CONTAINER_SIZE - j - 1);
                    if (i == lastContainerIndex) {
                        lastIndexInLastContainer--;
                    }
                    size--;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        for (int i = lastContainerIndex; i >= firstContainerIndex; i--) {
            Container container = containers[i];
            for (int j = (i == lastContainerIndex ? lastIndexInLastContainer : DEFAULT_CONTAINER_SIZE - 1); j >= 0; j--) {
                if (Objects.equals(o, container.elements[j])) {
                    System.arraycopy(container.elements, j + 1, container.elements, j, DEFAULT_CONTAINER_SIZE - j - 1);
                    if (i == lastContainerIndex) {
                        lastIndexInLastContainer--;
                    }
                    size--;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for (E e : c) {
            addLast(e);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            while (remove(o)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        boolean modified = false;
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            if (filter.test(iterator.next())) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            if (!c.contains(iterator.next())) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        containers = new Container[maxSize / DEFAULT_CONTAINER_SIZE + 1];
        containers[0] = new Container();
        firstContainerIndex = 0;
        lastContainerIndex = 0;
        firstIndexInFirstContainer = 0;
        lastIndexInLastContainer = 0;
        size = 0;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size, Spliterator.ORDERED | Spliterator.NONNULL);
    }

    @Override
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        for (int i = firstContainerIndex; i <= lastContainerIndex; i++) {
            Container container = containers[i];
            for (int j = (i == firstContainerIndex ? firstIndexInFirstContainer : 0); j < DEFAULT_CONTAINER_SIZE; j++) {
                if (Objects.equals(o, container.elements[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new TripletDequeIterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        for (int i = firstContainerIndex; i <= lastContainerIndex; i++) {
            Container container = containers[i];
            for (int j = (i == firstContainerIndex ? firstIndexInFirstContainer : 0); j < DEFAULT_CONTAINER_SIZE; j++) {
                if (container.elements[j] != null) {
                    action.accept(container.elements[j]);
                }
            }
        }
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        int index = 0;
        for (int i = firstContainerIndex; i <= lastContainerIndex; i++) {
            Container container = containers[i];
            for (int j = (i == firstContainerIndex ? firstIndexInFirstContainer : 0); j < DEFAULT_CONTAINER_SIZE; j++) {
                if (container.elements[j] != null) {
                    array[index++] = container.elements[j];
                }
            }
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        int index = 0;
        for (int i = firstContainerIndex; i <= lastContainerIndex; i++) {
            Container container = containers[i];
            for (int j = (i == firstContainerIndex ? firstIndexInFirstContainer : 0); j < DEFAULT_CONTAINER_SIZE; j++) {
                if (container.elements[j] != null) {
                    a[index++] = (T) container.elements[j];
                }
            }
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return toArray(generator.apply(size));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new TripletDequeDescendingIterator();
    }

    private class TripletDequeIterator implements Iterator<E> {
        private int currentContainerIndex = firstContainerIndex;
        private int currentIndex = firstIndexInFirstContainer;

        @Override
        public boolean hasNext() {
            return currentContainerIndex < lastContainerIndex || (currentContainerIndex == lastContainerIndex && currentIndex <= lastIndexInLastContainer);
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E element = containers[currentContainerIndex].elements[currentIndex];
            currentIndex++;
            if (currentIndex == DEFAULT_CONTAINER_SIZE) {
                currentContainerIndex++;
                currentIndex = 0;
            }
            return element;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class TripletDequeDescendingIterator implements Iterator<E> {
        private int currentContainerIndex = lastContainerIndex;
        private int currentIndex = lastIndexInLastContainer;

        @Override
        public boolean hasNext() {
            return currentContainerIndex > firstContainerIndex || (currentContainerIndex == firstContainerIndex && currentIndex >= firstIndexInFirstContainer);
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E element = containers[currentContainerIndex].elements[currentIndex];
            currentIndex--;
            if (currentIndex == -1) {
                currentContainerIndex--;
                currentIndex = DEFAULT_CONTAINER_SIZE - 1;
            }
            return element;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}