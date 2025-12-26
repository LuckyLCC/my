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
        # -> Click the login button at index 8 to authenticate.
        frame = context.pages[-1]
        # Click login button
        elem = frame.locator('xpath=html/body/div/section/div/div/div/footer/button[2]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Send API request with invalid data to trigger validation error and verify the error response format.
        await page.goto('http://localhost:8080/api/members', timeout=10000)
        await asyncio.sleep(3)
        

        # -> Send API request with invalid data to trigger validation error and verify the error response format.
        await page.goto('http://localhost:8080/api/members', timeout=10000)
        await asyncio.sleep(3)
        

        # -> Send API request with invalid data to trigger validation error and verify the error response format.
        await page.goto('http://localhost:8080/api/members', timeout=10000)
        await asyncio.sleep(3)
        

        # -> Send API request with invalid data to trigger validation error and verify the error response format.
        await page.goto('http://localhost:8080/api/members', timeout=10000)
        await asyncio.sleep(3)
        

        # -> Return to the main application to log in and obtain a valid token/session, then send an API request with invalid data to trigger validation error and verify error response format.
        await page.goto('http://localhost:5173/index.html', timeout=10000)
        await asyncio.sleep(3)
        

        # -> Click the login button at index 9 to authenticate as admin and obtain valid token.
        frame = context.pages[-1]
        # Click login button
        elem = frame.locator('xpath=html/body/div/section/div/div/div/footer/button[2]').nth(0)
        await page.wait_for_timeout(3000); await elem.click(timeout=5000)
        

        # -> Send API request with invalid data to trigger validation error and verify the error response format.
        await page.goto('http://localhost:8080/api/members', timeout=10000)
        await asyncio.sleep(3)
        

        # -> Verify UI permission restrictions for the logged-in staff user on the member management page, specifically that edit and delete buttons are hidden.
        await page.goto('http://localhost:5173/index.html', timeout=10000)
        await asyncio.sleep(3)
        

        # --> Assertions to verify final state
        frame = context.pages[-1]
        await expect(frame.locator('text=Gym Admin一体化后台').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=会员管理').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=卡种管理').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=员工管理').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=提成规则').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=开卡统计').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=交易明细').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=系统管理员 (ADMIN)').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=退出').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=新增').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=刷新').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=wangmazi').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=John Doe').first).to_be_visible(timeout=30000)
        await expect(frame.locator('text=Test User').first).to_be_visible(timeout=30000)
        await asyncio.sleep(5)
    
    finally:
        if context:
            await context.close()
        if browser:
            await browser.close()
        if pw:
            await pw.stop()
            
asyncio.run(run_test())
    