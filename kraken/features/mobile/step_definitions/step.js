const { Given, When, Then } = require("@cucumber/cucumber");
const assert = require("assert");

Then("I tap on element with id {string}", async function (text) {
  const element = await this.driver.$(
    `android=new UiSelector().text("${text}")`,
  );
  await element.click();
});

Then("I see the text {string}", async function (text) {
  const element = await this.driver.$(
    `android=new UiSelector().textContains("${text}")`,
  );
  await element.waitForDisplayed({ timeout: 5000 });
  return element;
});

Then("I tap on coordinates {int} {int}", async function (x, y) {
  await this.driver.touchAction({ action: "tap", x: x, y: y });
});

Then("I take a screenshot", async function () {
  await this.driver.saveScreenshot(`reports/screenshot_${Date.now()}.png`);
});

Then("I tap on element with accessibility id {string}", async function (id) {
  const element = await this.driver.$(`~${id}`);
  await element.click();
});

Then("I scroll up", async function () {
  await this.driver.execute("mobile: scrollGesture", {
    left: 100,
    top: 300,
    width: 800,
    height: 800,
    direction: "up",
    percent: 3,
  });
});

Then("I scroll down", async function () {
  await this.driver.execute("mobile: scrollGesture", {
    left: 100,
    top: 300,
    width: 800,
    height: 800,
    direction: "down",
    percent: 3,
  });
});

Then("I don't see the text {string}", async function (text) {
  const elements = await this.driver.$$(
    `android=new UiSelector().textContains("${text}")`,
  );
  assert.strictEqual(elements.length, 0);
});

Then("I type {string}", async function (text) {
  const element = await this.driver.$(`android=new UiSelector().focused(true)`);
  await element.setValue(text);
});

Then("I tap on element with text containing {string}", async function (text) {
  const element = await this.driver.$(
    `android=new UiSelector().textContains("${text}")`,
  );
  await element.click();
});

Then("I select role if needed", async function () {
  const elements = await this.driver.$$(
    `android=new UiSelector().textContains("Visitante")`,
  );
  if (elements.length > 0) {
    await elements[0].click();
  }
});

Then("I select role collector if needed", async function () {
  const elements = await this.driver.$$(
    `android=new UiSelector().textContains("Coleccionista")`,
  );
  if (elements.length > 0) {
    await elements[0].click();
  }
});

Then("I clear the text of element with id {string}", async function (id) {
  const element = await this.driver.$(
    `android=new UiSelector().resourceIdMatches(".*${id}.*")`,
  );
  await element.clearValue();
});

Then("I type {string} on element with id {string}", async function (text, id) {
  const allEditTexts = await this.driver.$$(
    `android=new UiSelector().className("android.widget.EditText")`,
  );
  const allContainers = await this.driver.$$(
    `android=new UiSelector().descriptionContains("${id}")`,
  );

  if (allContainers.length === 0)
    throw new Error(`No container found for id: ${id}`);

  const boundsStr = await allContainers[0].getAttribute("bounds");
  const match = boundsStr.match(/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/);
  const [, x1, y1, x2, y2] = match.map(Number);

  for (const editText of allEditTexts) {
    const etBounds = await editText.getAttribute("bounds");
    const etMatch = etBounds.match(/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/);
    const [, ex1, ey1, ex2, ey2] = etMatch.map(Number);
    const centerX = (ex1 + ex2) / 2;
    const centerY = (ey1 + ey2) / 2;
    if (centerX >= x1 && centerX <= x2 && centerY >= y1 && centerY <= y2) {
      await editText.click();
      await editText.setValue(text);
      return;
    }
  }
  throw new Error(`No EditText found within container: ${id}`);
});

Then("I ensure collector role", async function () {
  const isCollector = await this.driver.$$(
    `android=new UiSelector().textContains("Coleccionista")`,
  );
  if (isCollector.length > 0) return;

  const menu = await this.driver.$(`~Abrir menú`);
  await menu.click();
  await this.driver.pause(1500);
  const btn = await this.driver.$(
    `android=new UiSelector().textContains("Hacerse coleccionista")`,
  );
  await btn.click();
  await this.driver.pause(1500);
});
