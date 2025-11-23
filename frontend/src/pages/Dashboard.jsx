import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getDocuments, deleteDocument, uploadDocument } from "../services/api";
import {
  FileText,
  Upload,
  MessageSquare,
  Trash2,
  LogOut,
  Loader,
  CheckCircle,
  AlertCircle,
} from "lucide-react";

export default function Dashboard() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    try {
      const response = await getDocuments();
      setDocuments(response.data.content);
    } catch (error) {
      console.error("Erro ao carregar documentos:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.type !== "application/pdf") {
      alert("Apenas arquivos PDF são aceitos");
      return;
    }

    setUploading(true);
    try {
      await uploadDocument(file);
      await loadDocuments();
      e.target.value = "";
    } catch (error) {
      alert(error.response?.data?.message || "Erro ao fazer upload");
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm("Tem certeza que deseja deletar este documento?")) return;

    try {
      await deleteDocument(id);
      await loadDocuments();
    } catch (error) {
      console.error("Erro ao deletar documento:", error);
      alert("Erro ao deletar documento");
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case "READY":
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case "PROCESSING":
        return <Loader className="w-5 h-5 text-blue-500 animate-spin" />;
      case "ERROR":
        return <AlertCircle className="w-5 h-5 text-red-500" />;
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <FileText className="w-8 h-8 text-indigo-600" />
            <div>
              <h1 className="text-2xl font-bold text-gray-900">DocChat AI</h1>
              <p className="text-sm text-gray-600">Olá, {user?.name}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center space-x-2 text-gray-600 hover:text-gray-900"
          >
            <LogOut className="w-5 h-5" />
            <span>Sair</span>
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Upload Section */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Upload de Documento
          </h2>
          <div className="flex items-center justify-center w-full">
            <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-gray-300 border-dashed rounded-lg cursor-pointer bg-gray-50 hover:bg-gray-100">
              <div className="flex flex-col items-center justify-center pt-5 pb-6">
                <Upload className="w-8 h-8 text-gray-400 mb-2" />
                <p className="text-sm text-gray-600">
                  {uploading
                    ? "Processando..."
                    : "Clique para fazer upload de um PDF"}
                </p>
              </div>
              <input
                type="file"
                className="hidden"
                accept=".pdf"
                onChange={handleUpload}
                disabled={uploading}
              />
            </label>
          </div>
        </div>

        {/* Documents List */}
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Meus Documentos
          </h2>

          {loading ? (
            <div className="flex justify-center py-8">
              <Loader className="w-8 h-8 text-indigo-600 animate-spin" />
            </div>
          ) : documents.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              Nenhum documento ainda. Faça upload de um PDF para começar!
            </div>
          ) : (
            <div className="space-y-3">
              {documents.map((doc) => (
                <div
                  key={doc.id}
                  className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:border-indigo-300 transition"
                >
                  <div className="flex items-center space-x-3 flex-1">
                    <FileText className="w-8 h-8 text-gray-400" />
                    <div className="flex-1">
                      <h3 className="font-medium text-gray-900">
                        {doc.originalFilename}
                      </h3>
                      <p className="text-sm text-gray-500">
                        {doc.pageCount} páginas •{" "}
                        {(doc.fileSize / 1024).toFixed(1)} KB
                      </p>
                    </div>
                    {getStatusIcon(doc.status)}
                  </div>
                  <div className="flex items-center space-x-2">
                    {doc.status === "READY" && (
                      <button
                        onClick={() => navigate(`/chat/${doc.id}`)}
                        className="p-2 text-indigo-600 hover:bg-indigo-50 rounded-lg"
                      >
                        <MessageSquare className="w-5 h-5" />
                      </button>
                    )}
                    <button
                      onClick={() => handleDelete(doc.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
