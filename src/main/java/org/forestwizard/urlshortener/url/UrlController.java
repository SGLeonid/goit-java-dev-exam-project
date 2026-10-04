package org.forestwizard.urlshortener.url;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class UrlController {
    private static final String TEMPLATE_NOT_FOUND = "not_found";
    private static final String TEMPLATE_EXPIRED = "expired";

    private final UrlService urlService;

    @GetMapping("/link/{uniqueId}")
    public RedirectView getUrl(@PathVariable("uniqueId") String uniqueId) {
        String url = urlService.getOriginalUrl(uniqueId);
        return new RedirectView(url, false);
    }

    @GetMapping("/notfound")
    public ModelAndView notFound() {
        ModelAndView view = new ModelAndView(TEMPLATE_NOT_FOUND);
        view.setStatus(HttpStatus.NOT_FOUND);
        return view;
    }

    @GetMapping("/expired")
    public ModelAndView expired() {
        ModelAndView view = new ModelAndView(TEMPLATE_EXPIRED);
        view.setStatus(HttpStatus.BAD_REQUEST);
        return view;
    }
}
