import SwiftUI
import shared

struct ContentView: View {
    @StateObject var viewModel = StopwatchViewModelWrapper()
    
    var body: some View {
        VStack {
            Text(viewModel.uiState.formattedTime)
                .padding()
            
            HStack(spacing: 8) {
                Button(action: viewModel.onStartClicked, label: {
                    Text("Start")
                })
                Button(action: viewModel.onStopClicked, label: {
                    Text("Stop")
                })
            }
        }
    }
}

class StopwatchViewModelWrapper: ObservableObject {
    private let viewModel = StopwatchViewModel()
    private var observer: Cancellable?
    
    @Published var uiState: StopwatchUiState = StopwatchUiState(currentTimeMillis: 0, initialTimeMillis: 0)
    
    func onStartClicked() {
        viewModel.onStartClicked()
    }
    
    func onStopClicked() {
        viewModel.onStopClicked()
    }
    
    init() {
        observer = viewModel.uiState.collect {
            [weak self] value in self?.uiState = value
        }
    }
    
    deinit {
        observer?.cancel()
        viewModel.onDestroy()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
