package org.unidal.net.transport;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.net.ClientTransport;

@Named(type = ClientTransport.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultClientTransport implements ClientTransport {
   @Inject
   private ClientTransportHandler m_handler;

   private ClientTransportDescriptor m_desc = new ClientTransportDescriptor();

   @Override
   public ClientTransport connect(String host, int port) {
      m_desc.setRemoteAddresses(Arrays.asList(new InetSocketAddress(host, port)));
      return this;
   }

   @Override
   public ClientTransport handler(String name, ChannelHandler handler) {
      m_desc.addHandler(name, handler);
      return this;
   }

   @Override
   public ClientTransport name(String name) {
      m_desc.setName(name);
      return this;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> ClientTransport option(ChannelOption<T> option, T value) {
      m_desc.getOptions().put((ChannelOption<Object>) option, value);
      return this;
   }

   @Override
   public ClientTransport start() {
      m_desc.validate();
      m_handler.setDescriptor(m_desc);

      Threads.forGroup(m_desc.getName()).start(m_handler);
      return this;
   }

   @Override
   public void stop(int timeout, TimeUnit unit) throws InterruptedException {
      m_handler.shutdown();
      m_handler.awaitTermination(timeout, unit);
   }

   @Override
   public ClientTransport withThreads(int threads) {
      m_desc.setThreads(threads);
      return this;
   }

   @Override
   public boolean write(Object message) {
      return m_handler.write(message);
   }
}
