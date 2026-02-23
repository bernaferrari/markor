import UIKit
import shared

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        
        window = UIWindow(frame: UIScreen.main.bounds)
        
        // Create the Compose UIViewController from shared module
        let rootViewController = MainViewControllerKt.MainViewController()
        
        window?.rootViewController = rootViewController
        window?.makeKeyAndVisible()
        
        return true
    }
}
