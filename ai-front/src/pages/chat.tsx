import { useState, useEffect, useRef } from 'react';
import styles from '../styles/chat.module.css';

const ChatPage = () => {
  // チャットメッセージの配列：ユーザーとシステム（APIのレスポンス）のメッセージを管理
  const [messages, setMessages] = useState<{ sender: 'user' | 'system'; text: string }[]>([]);
  // 入力欄のテキスト
  const [input, setInput] = useState('');
  // チャットウィンドウのスクロール制御用 ref
  const chatWindowRef = useRef<HTMLDivElement>(null);
  const [isLoading, setIsLoading] = useState(false);

  // 提案ボタン押下時の処理（/suggest?text= API 呼び出し）
  const handleSuggest = async () => {
    if (!input.trim()) return;
    // ユーザー入力をチャットに追加（右側表示）
    setMessages(prev => [...prev, { sender: 'user', text: input }]);

    try {
      const response = await fetch(`http://localhost:8080/suggest?text=${encodeURIComponent(input)}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      // suggestedKeywordsが存在すればその内容を表示
      const resultText = data.suggestedKeywords
        ? `関連するキーワードをいくつかご提案します。\n${data.suggestedKeywords}\n\n気になるものがあれば検索に追加してみてください。`
        : '提案結果がありません。';
      // システムメッセージとして追加（左側表示）
      setMessages(prev => [...prev, { sender: 'system', text: resultText }]);
    } catch (error) {
      console.error(error);
      setMessages(prev => [...prev, { sender: 'system', text: '提案APIエラーが発生しました。' }]);
    }
    // 入力欄は空にする
    setInput('');
  };

  // 調査ボタン押下時の処理（/search?query= API 呼び出し）
  const handleSearch = async () => {
    if (!input.trim()) return;
    setMessages(prev => [...prev, { sender: 'user', text: input }]);
    setIsLoading(true); // ローディング開始

    try {
      const response = await fetch(`http://localhost:8080/search?query=${encodeURIComponent(input)}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      const resultText = data.finalSummary
        ? `以下に調査結果を表示します。\n\n${data.finalSummary}`
        : '調査結果がありません。';
      setMessages(prev => [...prev, { sender: 'system', text: resultText }]);
    } catch (error) {
      console.error(error);
      setMessages(prev => [...prev, { sender: 'system', text: '調査APIエラーが発生しました。' }]);
    } finally {
      setIsLoading(false); // ローディング終了
    }
    setInput('');
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInput(e.target.value);
    const textarea = e.target;

    // 高さをリセットしてから内容に合わせて高さを調整
    textarea.style.height = 'auto';
    textarea.style.height = `${Math.min(textarea.scrollHeight, 200)}px`; // 最大200pxに制限
  };

  // メッセージが更新されたらチャットウィンドウを下までスクロール
  useEffect(() => {
    if (chatWindowRef.current) {
      chatWindowRef.current.scrollTop = chatWindowRef.current.scrollHeight;
    }
  }, [messages]);

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '1rem' }}>
      {/* タイトルバー */}
      <header style={{ textAlign: 'center', marginBottom: '1rem' }}>
        <h1>リサーチシステム</h1>
      </header>

      {/* チャットウィンドウ */}
      <div
        ref={chatWindowRef}
        style={{
          border: '1px solid #ddd',
          height: '400px',
          overflowY: 'auto',
          padding: '1rem',
          display: 'flex',
          flexDirection: 'column',
          borderRadius: '10px',
          boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
        }}
      >
        {messages.map((msg, idx) => (
          <div
            key={idx}
            style={{
              alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
              margin: '0.5rem 0',
              maxWidth: '70%'
            }}
          >
            <div
              style={{
                padding: '0.5rem 1rem',
                borderRadius: '1rem',
                background: msg.sender === 'user' ? '#DCF8C6' : '#FFF',
                border: '1px solid #ccc',
                whiteSpace: 'pre-line',  // 改行を適用
              }}
            >
              {msg.text}
            </div>
          </div>
        ))}

        {/* ローディングスピナー（メッセージのすぐ下） */}
        {isLoading && (
          <div
            style={{
              marginTop: '10px', // メッセージの直後に表示
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              gap: '10px',
            }}
          >
            <div className={styles.spinner} />
            <span>調査中です...</span>
          </div>
        )}
      </div>

      {/* 入力エリア */}
      <div style={{ marginTop: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <textarea
          value={input}
          onChange={handleInputChange}
          placeholder="テキストを入力"
          style={{ flex: 1, padding: '0.5rem', fontSize: '16px', resize: 'none', }}
        />
        <button onClick={handleSuggest} style={{ padding: '0.5rem 1rem', fontSize: '16px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '5px' }}>
          提案
        </button>
        <button onClick={handleSearch} disabled={isLoading} style={{ padding: '0.5rem 1rem', fontSize: '16px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '5px' }}>
          {isLoading ? '調査中' : '調査'}
        </button>
      </div>
    </div>
  );
};

export default ChatPage;
