/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package africa.loreon.pay.ui.services.checkout;

import africa.loreon.pay.ui.client.checkout.CheckoutClient;
import africa.loreon.pay.ui.client.checkout.models.CheckoutRequest;
import africa.loreon.pay.ui.services.carts.CartsService;
import africa.loreon.pay.ui.services.carts.model.Cart;
import africa.loreon.pay.ui.services.checkout.model.Checkout;
import africa.loreon.pay.ui.services.checkout.model.CheckoutMapper;
import africa.loreon.pay.ui.services.checkout.model.CheckoutSubmitted;
import africa.loreon.pay.ui.services.checkout.model.ShippingAddress;
import africa.loreon.pay.ui.util.RetryUtils;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class KiotaCheckoutService implements CheckoutService {

  private final CheckoutClient checkoutClient;
  private final CheckoutMapper mapper;
  private final CartsService cartsService;

  public KiotaCheckoutService(
    CheckoutClient checkoutClient,
    CheckoutMapper mapper,
    CartsService cartsService
  ) {
    this.checkoutClient = checkoutClient;
    this.mapper = mapper;

    this.cartsService = cartsService;
  }

  @Override
  public Mono<Checkout> get(String sessionId) {
    return Mono.just(
      this.checkoutClient.checkout().byCustomerId(sessionId).get()
    )
      .retryWhen(RetryUtils.apiClientRetrySpec("get checkout"))
      .map(mapper::checkout);
  }

  @Override
  public Mono<Checkout> create(String sessionId) {
    Mono<Cart> cart = cartsService.getCart(sessionId);

    return cart.flatMap(c -> {
      CheckoutRequest request = new CheckoutRequest();
      request.setItems(
        c
          .getItems()
          .stream()
          .map(mapper::fromCartItem)
          .collect(Collectors.toList())
      );

      return Mono.just(
        this.checkoutClient.checkout()
          .byCustomerId(sessionId)
          .update()
          .post(request)
      ).map(mapper::checkout);
    });
  }

  @Override
  public Mono<Checkout> shipping(
    String sessionId,
    ShippingAddress shippingAddress
  ) {
    return Mono.just(
      this.checkoutClient.checkout().byCustomerId(sessionId).get()
    ).flatMap(c -> {
      CheckoutRequest request = new CheckoutRequest();
      request.setShippingAddress(
        this.mapper.clientShippingAddress(shippingAddress)
      );
      request.setItems(
        c.getItems().stream().map(i -> mapper.modelitem(i)).toList()
      );

      return Mono.just(
        this.checkoutClient.checkout()
          .byCustomerId(sessionId)
          .update()
          .post(request)
      ).map(mapper::checkout);
    });
  }

  @Override
  public Mono<Checkout> delivery(String sessionId, String token) {
    return Mono.just(
      this.checkoutClient.checkout().byCustomerId(sessionId).get()
    ).flatMap(c -> {
      CheckoutRequest request = new CheckoutRequest();
      request.setShippingAddress(c.getShippingAddress());
      request.setDeliveryOptionToken(token);
      request.setItems(
        c.getItems().stream().map(i -> mapper.modelitem(i)).toList()
      );

      return Mono.just(
        this.checkoutClient.checkout()
          .byCustomerId(sessionId)
          .update()
          .post(request)
      ).map(mapper::checkout);
    });
  }

  @Override
  public Mono<CheckoutSubmitted> submit(String sessionId) {
    return Mono.just(
      this.checkoutClient.checkout().byCustomerId(sessionId).submit().post()
    ).zipWith(this.cartsService.deleteCart(sessionId), (e, f) -> {
      var result = mapper.submitted(e);
      return result;
    });
  }
}
