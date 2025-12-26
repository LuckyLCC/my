import asyncio
from playwright import async_api
from playwright.async_api import expect

async def run_test():
    pw = None
    browser = None
    context = None
    
    try:
        # Start a Playwright session in asynchronous mode
        pw = await async_api.async_playwright().start()
        
        # Launch a Chromium browser in headless mode with custom arguments
        browser = await pw.chromium.launch(
            headless=True,
            args=[
                "--window-size=1280,720",         # Set the browser window size
                "--disable-dev-shm-usage",        # Avoid using /dev/shm which can cause issues in containers
                "--ipc=host",                     # Use host-level IPC for better stability
                "--single-process"                # Run the browser in a single process mode
            ],
        )
        
        # Create a new browser context (like an incognito window)
        context = await browser.new_context()
        context.set_default_timeout(5000)
        
        # Open a new page in the browser context
        page = await context.new_page()
        
        # Navigate to your target URL and wait until the network request is committed
        await page.goto("http://localhost:5173/index.html", wait_until="commit", timeout=10000)
        
        # Wait for the main page to reach DOMContentLoaded state (optional for stability)
        try:
            await page.wait_for_load_state("domcontentloaded", timeout=3000)
        except async_api.Error:
            pass
        
        # Iterate through all iframes and wait for them to load as well
        for frame in page.frames:
            try:
                await frame.wait_for_load_state("domcontentloaded", timeout=3000)
            except async_api.Error:
                pass
        
        # Interact with the page elements to simulate user flow
        # -> Click the login button to authenticate.
        frame = context.pages[-1]
        # Click the 登录 (Login) button to authenticate.
        elem = frame.locator('xpath=html/body/div/section/div/div/div/footer/button[2]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click on '开卡统计' (Card Opening Statistics) in the left navigation menu to access monthly card opening summary.
        frame = context.pages[-1]
        # Click on '开卡统计' (Card Opening Statistics) in the left navigation menu
        elem = frame.locator('xpath=html/body/div/section/div/div/div/div/form/div[2]/div/div/div/input').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click the 登录 (Login) button in the token request modal to re-authenticate and obtain token.
        frame = context.pages[-1]
        # Click the 登录 (Login) button in the token request modal to re-authenticate and obtain token.
        elem = frame.locator('xpath=html/body/div/section/div/div/div/footer/button[2]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click on '开卡统计' (Card Opening Statistics) in the left navigation menu to access monthly card opening summary.
        frame = context.pages[-1]
        # Click on '开卡统计' (Card Opening Statistics) in the left navigation menu
        elem = frame.locator('xpath=html/body/div/section/aside/ul/li[5]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click the 登录 (Login) button in the modal to renew the token/session and access the '开卡统计' (Card Opening Statistics) page.
        frame = context.pages[-1]
        # Click the 登录 (Login) button in the token renewal modal to authenticate and obtain a new token.
        elem = frame.locator('xpath=html/body/div/section/div/div/div/footer/button[2]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click the 登录 (Login) button in the modal to renew the token/session and access the '开卡统计' (Card Opening Statistics) page.
        frame = context.pages[-1]
        # Click the 登录 (Login) button in the token renewal modal to authenticate and obtain a new token.
        elem = frame.locator('xpath=html/body/div/section/div/div/div/footer/button[2]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click on the '交易明细' (Transaction Details) tab in the left navigation menu to view transaction details and employee commission statistics.
        frame = context.pages[-1]
        # Click on '交易明细' (Transaction Details) in the left navigation menu
        elem = frame.locator('xpath=html/body/div/section/aside/ul/li[6]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click the 登录 (Login) button in the modal to renew the token/session and access the transaction details page.
        frame = context.pages[-1]
        # Click the 登录 (Login) button in the token renewal modal to authenticate and obtain a new token.
        elem = frame.locator('xpath=html/body/div/section/div/div/div/footer/button[2]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Navigate to '提成规则' (Commission Rules) or '员工管理' (Employee Management) to view employee commission statistics for verification.
        frame = context.pages[-1]
        # Click on '提成规则' (Commission Rules) in the left navigation menu to view employee commission statistics.
        elem = frame.locator('xpath=html/body/div/section/aside/ul/li[4]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click the 登录 (Login) button in the modal to renew the token/session and access the commission rules page.
        frame = context.pages[-1]
        # Click the 登录 (Login) button in the token renewal modal to authenticate and obtain a new token.
        elem = frame.locator('xpath=html/body/div/section/div/div/div/footer/button[2]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click the '开卡统计' (Card Opening Statistics) tab to return to the monthly card opening summary page and prepare for report export.
        frame = context.pages[-1]
        # Click on '开卡统计' (Card Opening Statistics) in the left navigation menu to access monthly card opening summary.
        elem = frame.locator('xpath=html/body/div/section/aside/ul/li[5]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click the 登录 (Login) button in the modal to renew the token/session and access the monthly card opening summary page.
        frame = context.pages[-1]
        # Click the 登录 (Login) button in the token renewal modal to authenticate and obtain a new token.
        elem = frame.locator('xpath=html/body/div/section/div/div/div/footer/button[2]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Click the '刷新' (Refresh) button to ensure data is up to date, then export the report to Excel format.
        frame = context.pages[-1]
        # Click the '刷新' (Refresh) button to update the monthly card opening summary data.
        elem = frame.locator('xpath=html/body/div/section/section/main/div/div/div[2]/button').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # --> Assertions to verify final state
        frame = context.pages[-1]
        await expect(frame.locator('text=开卡统计（只读）').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=月份：2025-12').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=总开卡数量：5').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=总开卡金额：¥58360.00').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=总提成金额：¥1167.20').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=系统管理员').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=刘畅').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=共4条记录（点击查看详情）').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=共1条记录（点击查看详情）').first).to_be_visible(timeout=30000)
        await asyncio.sleep(5)
    
    finally:
        if context:
            await context.close()
        if browser:
            await browser.close()
        if pw:
            await pw.stop()
            
asyncio.run(run_test())
    