import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


class RespostaAPI<T> {
    private final boolean sucesso;
    private final String mensagem;
    private final T dados;

    private RespostaAPI(boolean sucesso, String mensagem, T dados) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.dados = dados;
    }

    public static <T> RespostaAPI<T> sucesso(T dados, String mensagem) {
        return new RespostaAPI<>(true, mensagem, dados);
    }

    public static <T> RespostaAPI<T> falha(String mensagem) {
        return new RespostaAPI<>(false, mensagem, null);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public T getDados() {
        return dados;
    }

    @Override
    public String toString() {
        return String.format("Resposta{sucesso=%b, mensagem='%s', dados=%s}", sucesso, mensagem, dados);
    }
}


class Conteudo {
    private final String texto;
    private final byte[] midia; // Simula um arquivo de imagem ou vídeo

    public Conteudo(String texto, byte[] midia) {
        this.texto = texto;
        this.midia = midia;
    }

    public String getTexto() { return texto; }
    public byte[] getMidia() { return midia; }
}


class TwitterAPI {
    public String postTweet(String apiKey, String user, String tweetContent) {
        System.out.println("[TwitterAPI] Autenticando com a chave: " + apiKey);
        System.out.println("[TwitterAPI] Publicando tweet para o usuário @" + user + ": '" + tweetContent + "'");
        return "TweetID:" + UUID.randomUUID().toString(); // Retorna um ID de publicação
    }
}

class LinkedInAPI {
    static class Credentials {
        String email; String token;
        public Credentials(String email, String token) { this.email = email; this.token = token; }
    }
    static class PostData {
        String title; String body;
        public PostData(String title, String body) { this.title = title; this.body = body; }
    }

    public boolean shareUpdate(Credentials creds, PostData post) {
        System.out.println("[LinkedInAPI] Autenticando usuário: " + creds.email);
        System.out.println("[LinkedInAPI] Compartilhando atualização: '" + post.title + "'");
        return true;
    }
}


class InstagramAPI {
    public void publishPhoto(String authToken, byte[] photo, String caption) throws Exception {
        System.out.println("[InstagramAPI] Validando token de autenticação...");
        if (photo == null) {
            throw new Exception("[InstagramAPI] Erro: A publicação de fotos requer um arquivo de mídia.");
        }
        System.out.println("[InstagramAPI] Publicando foto com a legenda: '" + caption + "'");
    }
}


interface ISocialMediaAdapter {
    RespostaAPI<String> publicar(Conteudo conteudo);
    RespostaAPI<Boolean> autenticar(String usuario, String senha);
}


class TwitterAdapter implements ISocialMediaAdapter {
    private final TwitterAPI twitterApi;
    private String apiKey = "twitter_api_key_123"; // Simula configuração

    public TwitterAdapter() {
        this.twitterApi = new TwitterAPI();
    }

    @Override
    public RespostaAPI<String> publicar(Conteudo conteudo) {
        try {
            // Tradução do modelo de dados unificado para o formato da API específica
            String tweetId = twitterApi.postTweet(apiKey, "currentUser", conteudo.getTexto());
            return RespostaAPI.sucesso(tweetId, "Tweet publicado com sucesso!");
        } catch (Exception e) {
            // Tratamento de erro granular
            return RespostaAPI.falha("Erro ao publicar no Twitter: " + e.getMessage());
        }
    }

    @Override
    public RespostaAPI<Boolean> autenticar(String usuario, String senha) {
        System.out.println("[TwitterAdapter] Autenticação simulada para o Twitter.");
        return RespostaAPI.sucesso(true, "Autenticado no Twitter com sucesso.");
    }
}


class LinkedInAdapter implements ISocialMediaAdapter {
    private final LinkedInAPI linkedInApi;

    public LinkedInAdapter() {
        this.linkedInApi = new LinkedInAPI();
    }

    @Override
    public RespostaAPI<String> publicar(Conteudo conteudo) {
        try {
            // Tradução de dados para os modelos específicos da LinkedInAPI
            LinkedInAPI.Credentials creds = new LinkedInAPI.Credentials("user@email.com", "linkedin_token");
            LinkedInAPI.PostData post = new LinkedInAPI.PostData("Publicação Profissional", conteudo.getTexto());
            
            boolean sucesso = linkedInApi.shareUpdate(creds, post);
            if (sucesso) {
                return RespostaAPI.sucesso("LinkedInPost:" + UUID.randomUUID(), "Publicado no LinkedIn!");
            } else {
                return RespostaAPI.falha("API do LinkedIn retornou falha na publicação.");
            }
        } catch (Exception e) {
            return RespostaAPI.falha("Erro ao publicar no LinkedIn: " + e.getMessage());
        }
    }

    @Override
    public RespostaAPI<Boolean> autenticar(String usuario, String senha) {
        System.out.println("[LinkedInAdapter] Autenticação simulada para o LinkedIn.");
        return RespostaAPI.sucesso(true, "Autenticado no LinkedIn com sucesso.");
    }
}


class InstagramAdapter implements ISocialMediaAdapter {
    private final InstagramAPI instagramApi;
    private String authToken = "instagram_auth_token_xyz";

    public InstagramAdapter() {
        this.instagramApi = new InstagramAPI();
    }

    @Override
    public RespostaAPI<String> publicar(Conteudo conteudo) {
        try {
            // Instagram exige mídia, então adicionamos uma verificação.
            if (conteudo.getMidia() == null) {
                return RespostaAPI.falha("O Instagram requer uma imagem ou vídeo para publicar.");
            }
            instagramApi.publishPhoto(authToken, conteudo.getMidia(), conteudo.getTexto());
            return RespostaAPI.sucesso("InstaPost:" + UUID.randomUUID(), "Foto publicada no Instagram!");
        } catch (Exception e) {
            return RespostaAPI.falha("Erro ao publicar no Instagram: " + e.getMessage());
        }
    }

    @Override
    public RespostaAPI<Boolean> autenticar(String usuario, String senha) {
        System.out.println("[InstagramAdapter] Autenticação simulada para o Instagram.");
        return RespostaAPI.sucesso(true, "Autenticado no Instagram com sucesso.");
    }
}


enum Plataforma {
    TWITTER, LINKEDIN, INSTAGRAM
}


class SocialMediaFactory {
    // Cache para garantir que apenas uma instância de cada adapter seja criada (Thread-safe)
    private static final Map<Plataforma, ISocialMediaAdapter> cache = new ConcurrentHashMap<>();

    // Oculta o construtor para evitar instanciação
    private SocialMediaFactory() {}

    public static ISocialMediaAdapter criarAdaptador(Plataforma plataforma) {
        // Usa computeIfAbsent para garantir a criação atômica e segura em ambientes com múltiplas threads
        return cache.computeIfAbsent(plataforma, p -> {
            switch (p) {
                case TWITTER:
                    return new TwitterAdapter();
                case LINKEDIN:
                    return new LinkedInAdapter();
                case INSTAGRAM:
                    return new InstagramAdapter();
                default:
                    throw new IllegalArgumentException("Plataforma de mídia social desconhecida: " + p);
            }
        });
    }
}


class GerenciadorMidiaSocial {
    private final ISocialMediaAdapter adaptador;

    public GerenciadorMidiaSocial(Plataforma plataforma) {
        // O gerenciador usa a Factory para obter o adaptador correto
        this.adaptador = SocialMediaFactory.criarAdaptador(plataforma);
        System.out.println("\n--- Gerenciador configurado para: " + plataforma + " ---");
    }

    public void postarConteudo(Conteudo conteudo) {
        System.out.println("Tentando postar: '" + conteudo.getTexto() + "'");
        RespostaAPI<String> resposta = adaptador.publicar(conteudo);
        System.out.println("Resultado: " + resposta);
    }
}


public class SocialMediaIntegration {
    public static void main(String[] args) {
        // Criando diferentes tipos de conteúdo
        Conteudo postTexto = new Conteudo("Esta é uma publicação de texto via meu sistema unificado.", null);
        Conteudo postComImagem = new Conteudo("Confira esta incrível foto de paisagem!", new byte[]{1, 2, 3});

        // 1. Usando o gerenciador para postar no Twitter
        GerenciadorMidiaSocial gerenciadorTwitter = new GerenciadorMidiaSocial(Plataforma.TWITTER);
        gerenciadorTwitter.postarConteudo(postTexto);

        // 2. Usando o mesmo gerenciador para postar no LinkedIn
        GerenciadorMidiaSocial gerenciadorLinkedIn = new GerenciadorMidiaSocial(Plataforma.LINKEDIN);
        gerenciadorLinkedIn.postarConteudo(postTexto);

        // 3. Usando o gerenciador para postar no Instagram
        GerenciadorMidiaSocial gerenciadorInstagram = new GerenciadorMidiaSocial(Plataforma.INSTAGRAM);
        // Tentativa de postar sem imagem (deve falhar)
        gerenciadorInstagram.postarConteudo(postTexto);
        // Tentativa correta com imagem (deve funcionar)
        gerenciadorInstagram.postarConteudo(postComImagem);

        // 4. Demonstração do cache da Factory
        System.out.println("\n--- Verificando o cache da Factory ---");
        ISocialMediaAdapter twitter1 = SocialMediaFactory.criarAdaptador(Plataforma.TWITTER);
        ISocialMediaAdapter twitter2 = SocialMediaFactory.criarAdaptador(Plataforma.TWITTER);
        System.out.println("As duas instâncias do adapter do Twitter são as mesmas? " + (twitter1 == twitter2));
    }
}